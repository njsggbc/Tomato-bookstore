package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.configure.AlipayConfig;
import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import cn.edu.nju.TomatoMall.events.order.OrderCancelEvent;
import cn.edu.nju.TomatoMall.events.order.OrderRefundSuccessEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCancelEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentCreateEvent;
import cn.edu.nju.TomatoMall.events.payment.PaymentSuccessEvent;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.repository.PaymentRepository;
import cn.edu.nju.TomatoMall.service.PaymentService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

/**
 * 支付宝支付服务实现类
 * 实现PaymentService接口，处理支付宝支付相关业务逻辑
 */
@Slf4j
@Service
public class AlipayServiceImpl extends PaymentServiceImpl {

    // ====================================================================================
    // 常量
    // ====================================================================================

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    // 重试延迟时间(毫秒)
    private static final long RETRY_DELAY_MS = 2000;

    // ====================================================================================
    // 依赖
    // ====================================================================================

    private final AlipayConfig config;
    private final AlipayClient alipayClient;

    @Autowired
    public AlipayServiceImpl(AlipayConfig config,
                             AlipayClient alipayClient,
                             PaymentRepository paymentRepository,
                             ApplicationEventPublisher eventPublisher,
                             SecurityUtil securityUtil
    ) {
        super(paymentRepository, eventPublisher, securityUtil);
        this.config = config;
        this.alipayClient = alipayClient;
    }

    // ====================================================================================
    // 方法实现
    // ====================================================================================

    /**
     * 获取支付方式
     * @return 返回支付宝支付方式枚举
     */
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.ALIPAY;
    }

    /**
     * 执行支付操作
     * @param payment
     * @return 返回支付宝支付表单HTML字符串
     */
    @Override
    @Transactional
    public String createTrade(Payment payment) {
        try {
            // 创建支付宝支付页面请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // 设置支付完成后的回跳页面
            request.setReturnUrl(config.getReturnUrl());
            // 设置异步通知地址
            request.setNotifyUrl(config.getNotifyUrl());

            // 构建支付业务参数
            JSONObject bizContent = buildPaymentBizContent(payment);
            request.setBizContent(bizContent.toString());

            // 执行API调用
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                // 支付请求成功，设置支付方式和请求时间
                payment.setPaymentMethod(PaymentMethod.ALIPAY);
                payment.setPaymentRequestTime(LocalDateTime.now());
                paymentRepository.save(payment);
                // 安排支付超时处理
                schedulePaymentTimeout(payment);
                return response.getBody();
            }
            // 支付请求失败
            throw TomatoMallException.paymentFail(response.getSubMsg());
        } catch (AlipayApiException e) {
            log.error("支付宝API调用错误", e);
            throw TomatoMallException.paymentFail(e.getMessage());
        }
    }

    /**
     *
     * @param payment
     */
    @Override
    @Transactional
    public void closeTrade(Payment payment) {
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            request.setBizContent(new JSONObject().fluentPut("out_trade_no", payment.getId()).toString());
            alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("关闭交易失败", e);
        }
    }

    /**
     * 处理支付宝异步通知
     * @param request HTTP请求对象，包含支付宝回调参数
     * @return 处理结果，成功返回"success"，失败返回"fail"
     */
    @Override
    @Transactional
    public String processNotify(HttpServletRequest request) {
        // 转换请求参数为Map
        Map<String, String> params = convertParams(request);
        try {
            // 验证签名
            verifySignature(params);
            // 获取交易状态和订单号
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");

            // 根据交易状态处理
            switch (tradeStatus) {
                case "TRADE_SUCCESS":  // 交易成功
                case "TRADE_FINISHED": // 交易完成
                    return handleSuccessfulPayment(params);
                case "TRADE_CLOSED":   // 交易关闭
                    return handleClosedPayment(outTradeNo);
                case "WAIT_BUYER_PAY": // 等待买家付款
                    log.info("等待支付: {}", outTradeNo);
                    return "success";
                default:
                    log.warn("未知交易状态: {}", tradeStatus);
                    return "fail";
            }
        } catch (Exception e) {
            log.error("回调处理异常", e);
            return "fail";
        }
    }

    /**
     * 执行退款请求
     * @param order 订单对象
     * @param amount 退款金额
     * @param reason 退款原因
     */
    @Override
    @Transactional
    public void processRefund(Order order, BigDecimal amount, String reason) {
        // 执行退款请求
        AlipayTradeRefundResponse response;
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            // 构建退款业务参数
            request.setBizContent(buildRefundContent(order, amount, reason).toString());
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("退款请求失败", e);
            throw TomatoMallException.refundFail(e.getMessage());
        }

        // 处理退款响应
        if (response.isSuccess()) {
            // 退款成功，通知订单服务
            eventPublisher.publishEvent(new OrderRefundSuccessEvent(order, new BigDecimal(response.getRefundFee()), response.getTradeNo()));
        } else {
            // 退款失败，尝试重试
            // 判断是否需要重试
            if (shouldRetry(response)) {
                // 最多重试MAX_RETRY_COUNT次
                for (int i = 0; i < MAX_RETRY_COUNT; i++) {
                    try {
                        // 延迟一段时间后重试，延迟时间随重试次数增加
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS * (i + 1));
                        // 构建重试请求
                        AlipayTradeRefundRequest retryRequest = buildRetryRequest(order, response);
                        AlipayTradeRefundResponse retryResponse = alipayClient.execute(retryRequest);
                        if (retryResponse.isSuccess()) return;
                    } catch (Exception e) {
                        log.error("退款重试失败", e);
                    }
                }
            }
            // 所有重试都失败，抛出异常
            throw TomatoMallException.refundFail(response.getSubMsg());
        }
    }

    /**
     * 处理支付超时
     * @param payment 支付对象
     */
    @Override
    @Transactional
    public void processTimeOut(Payment payment) {
        // 还没有发起支付，直接更新状态为超时
        if (payment.getPaymentMethod() == null) {
            // 更新支付状态为超时
            payment.setStatus(PaymentStatus.TIMEOUT);
            paymentRepository.save(payment);
            // 通知所有关联订单支付取消
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付超时"));
            return;
        }
        // 查询交易状态
        AlipayTradeQueryResponse response = (AlipayTradeQueryResponse) queryTradeStatus(payment.getId());
        if (!isTradeSuccess(response)) {
            // 交易未成功，关闭交易
            closeTrade(payment);
            // 更新支付状态为超时
            payment.setStatus(PaymentStatus.TIMEOUT);
            paymentRepository.save(payment);
            // 通知所有关联订单支付取消
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付超时"));
        }
    }

    /**
     * 查询交易状态
     * @param paymentId 支付ID
     * @return 支付宝交易查询响应
     */
    @Override
    @Transactional(readOnly = true)
    public AlipayTradeQueryResponse processQueryTrade(String paymentId) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent(new JSONObject().fluentPut("out_trade_no", paymentId).toString());
            return alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw TomatoMallException.operationFail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询退款状态
     * @param paymentId 支付ID
     * @param orderNo 订单号
     * @return 支付宝退款查询响应
     */
    @Override
    @Transactional(readOnly = true)
    public AlipayTradeFastpayRefundQueryResponse processQueryRefund(String paymentId, String orderNo) {
        try {
            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            JSONObject content = new JSONObject()
                    .fluentPut("out_trade_no", paymentId)
                    .fluentPut("out_request_no", orderNo);
            request.setBizContent(content.toString());
            return alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw TomatoMallException.operationFail("查询失败：" + e.getMessage());
        }
    }

    // ====================================================================================
    // 创建交易辅助方法
    // ====================================================================================

    /**
     * 构建支付宝支付请求业务参数
     * @param payment 支付对象
     * @return 业务参数JSON对象
     */
    private JSONObject buildPaymentBizContent(Payment payment) {
        JSONObject content = new JSONObject();
        // 商户订单号，即支付ID
        content.put("out_trade_no", payment.getId());
        // 订单总金额，保留两位小数
        content.put("total_amount", payment.getAmount().setScale(2, RoundingMode.HALF_UP));
        // 订单标题
        content.put("subject", truncateSubject(payment.getOrders()));
        // 产品码，固定值
        content.put("product_code", "FAST_INSTANT_TRADE_PAY");
        // 支付超时时间
        content.put("timeout_express", PAYMENT_TIMEOUT);
        return content;
    }

    /**
     * 截断订单主题，确保不超过支付宝限制长度
     * @param orders 订单列表
     * @return 截断后的订单主题
     */
    private String truncateSubject(List<Order> orders) {
        String subject = orders.stream()
                .map(o -> "订单" + o.getOrderNo())
                .collect(Collectors.joining(","));
        // 支付宝限制主题长度为128个字符，超出则截断
        return subject.length() > 128 ? subject.substring(0, 125) + "..." : subject;
    }

    /**
     * 处理支付成功的通知
     * @param params 通知参数
     * @return 处理结果
     */
    private String handleSuccessfulPayment(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        // 获取支付信息
        Payment payment = paymentRepository.findById(outTradeNo)
                .orElseThrow(TomatoMallException::paymentNotFound);

        // 移除支付超时处理任务
        removeSchedulePaymentTimeout(payment);

        // 如果支付已经成功，避免重复处理
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return "success";
        }

        // 验证支付金额
        BigDecimal receivedAmount = new BigDecimal(params.get("total_amount"))
                .setScale(2, RoundingMode.HALF_UP);
        if (payment.getAmount().compareTo(receivedAmount) != 0) {
            log.error("金额不一致: 本地={} 实际={}", payment.getAmount(), receivedAmount);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return "fail";
        }

        // 更新支付状态为成功
        payment.setStatus(PaymentStatus.SUCCESS);
        // 记录支付宝交易号
        payment.setTradeNo(params.get("trade_no"));
        // 记录交易时间
        payment.setTransactionTime(LocalDateTime.now());
        paymentRepository.save(payment);

        // 通知关联订单支付成功
        eventPublisher.publishEvent(new PaymentSuccessEvent(payment));

        return "success";
    }

    /**
     * 处理交易关闭的通知
     * @param outTradeNo 商户订单号
     * @return 处理结果
     */
    private String handleClosedPayment(String outTradeNo) {
        paymentRepository.findById(outTradeNo).ifPresent(payment -> {
            // 移除支付超时处理任务
            removeSchedulePaymentTimeout(payment);
            // 更新支付状态为失败
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            // 通知所有关联订单支付失败
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "交易关闭"));
        });
        return "success";
    }

    // ====================================================================================
    // 回调处理辅助方法
    // ====================================================================================

    /**
     * 转换HTTP请求参数为Map
     * @param request HTTP请求对象
     * @return 参数Map
     */
    private Map<String, String> convertParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) ->
                params.put(key, String.join(",", values))
        );
        return params;
    }

    /**
     * 验证支付宝通知签名
     * @param params 通知参数
     * @throws AlipayApiException 签名验证失败时抛出异常
     */
    private void verifySignature(Map<String, String> params) throws AlipayApiException {
        boolean valid = AlipaySignature.rsaCheckV1(
                params,
                config.getAlipayPublicKey(),
                config.getCharset(),
                config.getSignType()
        );
        if (!valid) throw TomatoMallException.paymentFail("签名验证失败");
    }

    /**
     * 判断交易是否成功
     * @param response 交易查询响应
     * @return 是否成功
     */
    private boolean isTradeSuccess(AlipayTradeQueryResponse response) {
        return response != null && response.isSuccess() &&
                ("TRADE_SUCCESS".equals(response.getTradeStatus()) ||
                        "TRADE_FINISHED".equals(response.getTradeStatus()));
    }

    // ====================================================================================
    // 退款处理辅助方法
    // ====================================================================================

    /**
     * 构建退款业务参数
     * @param order 订单对象
     * @param amount 退款金额
     * @param reason 退款原因
     * @return 业务参数JSON对象
     */
    private JSONObject buildRefundContent(Order order, BigDecimal amount, String reason) {
        JSONObject content = new JSONObject();
        // 商户订单号
        content.put("out_trade_no", order.getPayment().getId());
        // 支付宝交易号
        content.put("trade_no", order.getPayment().getTradeNo());
        // 退款金额
        content.put("refund_amount", amount.setScale(2, RoundingMode.HALF_UP));
        // 退款原因
        content.put("refund_reason", reason);
        // 退款请求号，使用订单号
        content.put("out_request_no", order.getOrderNo());
        return content;
    }

    /**
     * 构建退款重试请求
     * @param order 订单对象
     * @param response 上一次退款响应
     * @return 退款请求对象
     */
    private AlipayTradeRefundRequest buildRetryRequest(Order order, AlipayTradeRefundResponse response) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject content = new JSONObject();
        content.put("out_trade_no", order.getPayment().getId());
        content.put("trade_no", order.getPayment().getTradeNo());
        content.put("refund_amount", response.getRefundFee());
        content.put("out_request_no", order.getOrderNo());
        request.setBizContent(content.toString());
        return request;
    }

    /**
     * 判断是否应该重试退款
     * @param response 退款响应
     * @return 是否应该重试
     */
    private boolean shouldRetry(AlipayTradeRefundResponse response) {
        String code = response.getSubCode();
        // 系统错误或业务错误才重试
        return code != null && (code.startsWith("ACQ.") || code.equals("SYSTEM_ERROR"));
    }
}