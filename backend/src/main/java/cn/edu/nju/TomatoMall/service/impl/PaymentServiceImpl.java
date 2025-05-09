package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import cn.edu.nju.TomatoMall.events.order.OrderCancelEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCancelEvent;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.repository.PaymentRepository;
import cn.edu.nju.TomatoMall.service.PaymentService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public abstract class PaymentServiceImpl implements PaymentService {
    // 支付超时时间，单位分钟
    protected static final int PAYMENT_TIMEOUT = 5;

    protected final PaymentRepository paymentRepository;
    protected final ApplicationEventPublisher eventPublisher;
    protected final SecurityUtil securityUtil;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, ApplicationEventPublisher eventPublisher, SecurityUtil securityUtil) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.securityUtil = securityUtil;
    }

    // 使用单线程的定时执行器服务
    private ScheduledExecutorService scheduler;

    // 存储支付ID和对应的超时任务
    private final Map<Integer, ScheduledFuture<?>> paymentTimeoutTasks = new ConcurrentHashMap<>();

    // 用于处理事务的模板
    @Autowired
    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        // 创建单线程调度器，用于处理支付超时任务
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "payment-timeout-thread");
            thread.setDaemon(true); // 设置为守护线程，不阻止JVM退出
            return thread;
        });
    }

    @PreDestroy
    public void cleanup() {
        // 应用关闭时，关闭调度器
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                // 等待当前任务完成
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private final Map<PaymentMethod, PaymentServiceImpl> PAYMENT_STRATEGY = new HashMap<>();

    @Autowired
    public void setPaymentStrategy(
            AlipayServiceImpl alipayServiceImpl
    ) {
        PAYMENT_STRATEGY.put(PaymentMethod.ALIPAY, alipayServiceImpl);
    }

    // ====================================================================================
    // 接口方法实现
    // ====================================================================================

    @Override
    @Transactional
    public String pay(int paymentId, PaymentMethod paymentMethod) {
        PaymentServiceImpl paymentStrategy = PAYMENT_STRATEGY.get(paymentMethod);
        if (paymentStrategy == null) {
            throw TomatoMallException.paymentFail("不支持的支付方式");
        }

        Payment payment = paymentRepository.findByIdAndUserId(paymentId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::paymentNotFound);
        validatePayment(payment);
        // 如果已有支付，先关闭交易
        if (payment.getPaymentNo() != null) {
            paymentStrategy.closeTrade(payment);
        }
        payment.setPaymentNo(String.valueOf(System.currentTimeMillis())); // 生成新的支付单号
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
    public void cancel(int paymentId) {
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
            payment.setPaymentNo(null);
            paymentRepository.save(payment);
            // 通知所有关联订单支付取消
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付取消"));
        } catch (Exception e) {
            throw TomatoMallException.paymentFail(e.getMessage());
        }
    }

    @Override
    @Transactional
    public String handlePaymentNotify(HttpServletRequest request, PaymentMethod paymentMethod) {
        return PAYMENT_STRATEGY.get(paymentMethod).processNotify(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Object queryTradeStatus(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNoAndUserId(paymentNo, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::paymentNotFound);
        return PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processQueryTrade(paymentNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Object queryRefundStatus(String paymentNo, String orderNo) {
        Payment payment = paymentRepository.findByPaymentNoAndUserId(paymentNo, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::paymentNotFound);

        return PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processQueryRefund(paymentNo, orderNo);
    }

    // ====================================================================================
    // 事件监听处理
    // ====================================================================================

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
     * 定时任务：检查支付超时订单
     */
    @Transactional
    @Scheduled(fixedRate = 60000) // 每5分钟执行一次
    public void checkTimeoutPayments() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT);

        // 查找未发起过支付且创建时间超过阈值的待支付订单
        List<Payment> noMethodTimeoutPayments = paymentRepository.findByStatusAndPaymentMethodIsNullAndCreateTimeBefore(
                PaymentStatus.PENDING, timeoutThreshold);

        // 查找已发起过支付但处理时间超过阈值的待支付订单
        List<Payment> methodTimeoutPayments = paymentRepository.findByStatusAndPaymentMethodIsNotNullAndPaymentRequestTimeBefore(
                PaymentStatus.PENDING, timeoutThreshold);

        for (Payment payment : noMethodTimeoutPayments) {
            try {
                // 移除已存在的超时任务（如果有）
                removeSchedulePaymentTimeout(payment);
                // 更新支付状态为超时
                payment.setStatus(PaymentStatus.TIMEOUT);
                paymentRepository.save(payment);
                // 通知所有关联订单支付取消
                eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付超时"));
            } catch (Exception e) {
                // 记录异常，但继续处理下一个支付
                e.printStackTrace();
            }
        }

        for (Payment payment : methodTimeoutPayments) {
            try {
                // 移除已存在的超时任务（如果有）
                removeSchedulePaymentTimeout(payment);
                // 支付超时处理
                PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processTimeout(payment);
            } catch (Exception e) {
                // 记录异常，但继续处理下一个支付
                e.printStackTrace();
            }
        }
    }

    /**
     * 安排支付超时处理
     * @param payment 支付对象
     */
    public void schedulePaymentTimeout(Payment payment) {
        if (payment == null || scheduler.isShutdown()) {
            return;
        }

        // 如果已经存在超时任务，先移除
        removeSchedulePaymentTimeout(payment);

        // 创建一个新的超时任务
        ScheduledFuture<?> task = scheduler.schedule(
                () -> {
                    try {
                        // 使用事务模板在新事务中执行超时处理
                        transactionTemplate.execute(status -> {
                            // 从数据库获取最新的支付信息
                            Payment freshPayment = paymentRepository.findById(payment.getId()).orElse(null);
                            handleTimeout(freshPayment);
                            return null;
                        });
                    } catch (Exception e) {
                        // 记录错误，但不影响其他任务执行
                        e.printStackTrace();
                    } finally {
                        // 无论成功失败，都从任务映射中移除
                        paymentTimeoutTasks.remove(payment.getId());
                    }
                },
                PAYMENT_TIMEOUT,
                TimeUnit.MINUTES
        );

        // 存储任务以便后续可以取消
        paymentTimeoutTasks.put(payment.getId(), task);
    }

    /**
     * 移除支付超时处理任务
     * @param payment 支付对象
     */
    public void removeSchedulePaymentTimeout(Payment payment) {
        if (payment == null) {
            return;
        }

        // 获取并取消超时任务
        ScheduledFuture<?> task = paymentTimeoutTasks.remove(payment.getId());
        if (task != null && !task.isDone() && !task.isCancelled()) {
            task.cancel(false); // 不中断正在执行的任务
        }
    }


    /**
     * 处理超时支付状态
     */
    @Transactional
    public void handleTimeout(Payment payment) {
        if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            if (payment.getPaymentMethod() != null) {
                PAYMENT_STRATEGY.get(payment.getPaymentMethod()).processTimeout(payment);
            } else {
                // 如果没有支付方式，直接更新支付状态为超时
                payment.setStatus(PaymentStatus.TIMEOUT);
                paymentRepository.save(payment);
                // 通知所有关联订单支付取消
                eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付超时"));
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
        BigDecimal diff = payment.getAmount().subtract(totalAmount).abs();
        if (diff.compareTo(new BigDecimal("0.01")) > 0) {
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

    public abstract void processTimeout(Payment payment);

    public abstract Object processQueryTrade(String paymentNo);

    public abstract Object processQueryRefund(String paymentNo, String orderNo);
}