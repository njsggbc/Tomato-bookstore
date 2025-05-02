package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import cn.edu.nju.TomatoMall.events.order.OrderCancelEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCancelEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCreateEvent;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.repository.PaymentRepository;
import cn.edu.nju.TomatoMall.service.PaymentService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public abstract class PaymentServiceImpl implements PaymentService {
    // 支付超时时间，单位分钟
    protected static final String PAYMENT_TIMEOUT = "5m";

    protected final PaymentRepository paymentRepository;
    protected final ApplicationEventPublisher eventPublisher;
    protected final SecurityUtil securityUtil;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, ApplicationEventPublisher eventPublisher, SecurityUtil securityUtil) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.securityUtil = securityUtil;
    }

    private final Map<PaymentMethod, PaymentServiceImpl> PAYMENT_STRATEGY = new HashMap<>();

    @Autowired
    public void setPaymentStrategy(List<PaymentServiceImpl> paymentStrategy) {
        for (PaymentServiceImpl strategy : paymentStrategy) {
            PAYMENT_STRATEGY.put(strategy.getPaymentMethod(), strategy);
        }
    }

    // 定时任务锁
    private final Map<String, Lock> paymentLocks = new HashMap<>();

    // ====================================================================================
    // 接口方法实现
    // ====================================================================================


    @Override
    public String pay(String paymentId, PaymentMethod paymentMethod) {
       PaymentServiceImpl paymentStrategy = PAYMENT_STRATEGY.get(paymentMethod);
       if (paymentStrategy == null) {
           throw TomatoMallException.paymentFail("不支持的支付方式");
       }

       Payment payment = paymentRepository.findByIdAndUserId(paymentId, securityUtil.getCurrentUser().getId())
               .orElseThrow(TomatoMallException::paymentNotFound);
       validatePayment(payment);
       payment.setPaymentMethod(paymentMethod);
       payment.setPaymentRequestTime(LocalDateTime.now());

       return paymentStrategy.createTrade(payment);
    }

    /**
     * 取消支付
     * @param paymentId 支付ID
     */
    @Override
    @Transactional
    public void cancel(String paymentId) {
        // 获取支付信息
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::paymentNotFound);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw TomatoMallException.paymentFail("支付状态不允许操作");
        }

        try {
            // 移除支付超时处理任务
            removeSchedulePaymentTimeout(payment);
            // 如果已经创建了支付宝交易，关闭交易
            if (payment.getPaymentMethod() != null) {
                PAYMENT_STRATEGY.get(payment.getPaymentMethod()).closeTrade(payment);
            }
            // 更新支付状态为已取消
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            // 通知所有关联订单支付取消
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付取消"));
        } catch (Exception e) {
            throw TomatoMallException.paymentFail(e.getMessage());
        }
    }

    @Override
    public String handlePaymentNotify(HttpServletRequest request, PaymentMethod paymentMethod) {
        return PAYMENT_STRATEGY.get(paymentMethod).processNotify(request);
    }

    @Override
    public Object queryTradeStatus(String paymentId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::paymentNotFound);
        return PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processQueryTrade(paymentId);
    }

    @Override
    public Object queryRefundStatus(String paymentId, String orderNo) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::paymentNotFound);

        return PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processQueryRefund(paymentId, orderNo);
    }

    // ====================================================================================
    // 事件监听处理
    // ====================================================================================

    /**
     * 处理支付创建事件
     * @param event 支付创建事件
     */
    @EventListener
    @Transactional
    public void handlePaymentCreate(PaymentCreateEvent event) {
        paymentRepository.save(event.getPayment());
        schedulePaymentTimeout(event.getPayment());
    }

    /**
     * 处理订单退款事件
     * @param event 退款事件
     */
    @EventListener
    @Transactional
    public void handleOrderRefund(OrderCancelEvent event) {
        // 验证退款参数
        validateRefund(event.getOrder(), event.getRefundAmount());

        PAYMENT_STRATEGY.get(event.getOrder().getPayment().getPaymentMethod())
                .processRefund(event.getOrder(), event.getRefundAmount(), event.getReason());
    }

    // ====================================================================================
    // 通用工具方法
    // ====================================================================================

    /**
     * 安排支付超时处理
     * 异步执行，在指定时间后检查支付状态
     * @param payment 支付对象
     */
    @Async
    public void schedulePaymentTimeout(Payment payment) {
        // 移除之前的超时处理任务
        removeSchedulePaymentTimeout(payment);

        CompletableFuture.runAsync(() -> {
            try {
                // 5分钟后检查支付状态
                TimeUnit.MINUTES.sleep(5);
                // 获取锁，确保并发安全
                Lock lock = paymentLocks.computeIfAbsent(payment.getId(), k -> new ReentrantLock());
                lock.lock();

                try {
                    // 检查支付是否仍处于等待状态
                    if (payment.getStatus() == PaymentStatus.PENDING) {
                        PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processTimeOut(payment);
                    }
                } finally {
                    // 释放锁
                    lock.unlock();
                    paymentLocks.remove(payment.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 定时检查支付超时
     * 每5分钟执行一次，检查所有等待状态的支付
     */
    @Scheduled(fixedRate = 300000)
    public void checkPaymentTimeout() {
        // 获取所有等待状态的支付
        paymentRepository.findByStatus(PaymentStatus.PENDING).forEach(payment -> {
            // 尝试获取锁，避免与其他任务冲突
            Lock lock = paymentLocks.computeIfAbsent(payment.getId(), k -> new ReentrantLock());
            if (lock.tryLock()) {
                try {
                    // 重新获取支付信息，确保数据最新
                    Payment refreshedPayment = paymentRepository.findById(payment.getId())
                            .orElse(null);
                    if (refreshedPayment == null) {
                        return;
                    }
                    // 检查是否超时(创建时间+5分钟或支付请求时间+5分钟)
                    if ((refreshedPayment.getPaymentMethod() == null &&
                            refreshedPayment.getCreateTime().plusMinutes(5).isBefore(LocalDateTime.now())) ||
                            (refreshedPayment.getStatus() == PaymentStatus.PENDING &&
                                    refreshedPayment.getPaymentRequestTime().plusMinutes(5).isBefore(LocalDateTime.now()))
                    ) {
                        PAYMENT_STRATEGY.get(refreshedPayment.getPaymentMethod()).processTimeOut(payment);
                    }
                } finally {
                    // 释放锁
                    lock.unlock();
                    paymentLocks.remove(payment.getId());
                }
            }
        });
    }

    /**
     * 移除支付超时处理任务
     * @param payment 支付对象
     */
    public void removeSchedulePaymentTimeout(Payment payment) {
        Lock existingLock = paymentLocks.remove(payment.getId());
        if (existingLock instanceof ReentrantLock) {
            ReentrantLock reentrantLock = (ReentrantLock) existingLock;
            // 如果锁被当前线程持有，则释放它
            if (reentrantLock.isHeldByCurrentThread()) {
                reentrantLock.unlock();
            }
        }
    }

    /**
     * 验证支付信息
     * 检查支付金额是否与订单总金额一致
     * @param payment 支付对象
     */
    private void validatePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw TomatoMallException.paymentFail("支付状态不允许操作");
        }
        // 计算订单总金额
        BigDecimal totalAmount = payment.getOrders().stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 比较支付金额与订单总金额
        if (payment.getAmount().compareTo(totalAmount) != 0) {
            throw TomatoMallException.paymentFail("金额校验失败");
        }
    }

    /**
     * 验证退款参数
     * @param order 订单对象
     * @param amount 退款金额
     */
    private void validateRefund(Order order, BigDecimal amount) {
        // 检查退款金额是否合法（大于0且不超过订单总额）
        if (amount.compareTo(BigDecimal.ZERO) <= 0 ||
                amount.compareTo(order.getTotalAmount()) > 0) {
            throw TomatoMallException.refundFail("退款金额无效");
        }
    }

    // ====================================================================================
    // 策略方法
    // ====================================================================================

    public abstract PaymentMethod getPaymentMethod();

    public abstract String createTrade(Payment payment);

    public abstract void closeTrade(Payment payment);

    public abstract String processNotify(HttpServletRequest request);

    public abstract void processRefund(Order order, BigDecimal amount, String reason);

    public abstract void processTimeOut(Payment payment);

    public abstract Object processQueryTrade(String paymentId);

    public abstract Object processQueryRefund(String paymentId, String orderNo);

}
