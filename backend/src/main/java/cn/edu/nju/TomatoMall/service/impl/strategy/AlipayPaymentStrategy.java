package cn.edu.nju.TomatoMall.service.impl.strategy;

import cn.edu.nju.TomatoMall.configure.AlipayConfig;
import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.repository.PaymentRepository;
import cn.edu.nju.TomatoMall.service.impl.events.payment.*;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.edu.nju.TomatoMall.service.impl.PaymentServiceImpl.PAYMENT_TIMEOUT;

/**
 * 支付宝支付策略实现
 * 支持商户分账、退款等功能
 */
@Slf4j
@Component
public class AlipayPaymentStrategy implements PaymentStrategy {
    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    // 重试延迟时间(毫秒)
    private static final long RETRY_DELAY_MS = 2000;

    private final AlipayConfig config;
    private final AlipayClient alipayClient;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public AlipayPaymentStrategy(AlipayConfig config,
                                 AlipayClient alipayClient,
                                 PaymentRepository paymentRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.config = config;
        this.alipayClient = alipayClient;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.ALIPAY;
    }

    /**
     * 创建支付宝支付交易
     * 自动处理商户分账，为每个非系统商店的订单添加分账信息
     * @param payment 支付对象，包含订单信息和金额
     * @return 支付跳转链接（支付宝页面URL）
     */
    @Override
    @Transactional
    public String createTrade(Payment payment) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(config.getReturnUrl());
            request.setNotifyUrl(config.getNotifyUrl());

            JSONObject bizContent = buildPaymentBizContent(payment);
            log.debug("支付请求业务参数: {}", bizContent.toString());
            request.setBizContent(bizContent.toString());

            if (config.getEncryptKey() != null && !config.getEncryptKey().isEmpty()) {
                request.setNeedEncrypt(true);
            }

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");
            if (response.isSuccess()) {
                payment.setPaymentMethod(PaymentMethod.ALIPAY);
                payment.setPaymentRequestTime(LocalDateTime.now());
                paymentRepository.save(payment);
                log.debug("支付请求成功，返回URL: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("支付请求失败: subCode={}, subMsg={}", response.getSubCode(), response.getSubMsg());
                if (isSplitAccountError(response.getSubCode())) {
                    throw TomatoMallException.paymentFail("商户收款账户异常，请稍后重试或联系店铺客服");
                } else {
                    throw TomatoMallException.paymentFail(response.getSubMsg());
                }
            }
        } catch (AlipayApiException e) {
            log.error("支付宝API调用错误", e);
            throw TomatoMallException.paymentFail(e.getMessage());
        }
    }

    /**
     * 关闭支付宝交易
     * @param payment 支付对象
     */
    @Override
    @Transactional
    public void closeTrade(Payment payment) {
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            request.setBizContent(new JSONObject().fluentPut("out_trade_no", payment.getPaymentNo()).toString());

            if (config.getEncryptKey() != null && !config.getEncryptKey().isEmpty()) {
                request.setNeedEncrypt(true);
            }

            alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            log.error("关闭交易失败", e);
        }
    }

    /**
     * 处理支付宝异步通知
     * 验证签名并根据交易状态更新支付记录
     * @param request HTTP请求对象，包含支付宝回调参数
     * @return 处理结果，成功返回"success"，失败返回"fail"
     */
    @Override
    @Transactional
    public String processPaymentNotify(HttpServletRequest request) {
        Map<String, String> params = convertParams(request);
        try {
            log.info("接收到支付宝异步通知");

            if (!config.verifySignature(params)) {
                log.error("签名验证失败: {}", params);
                throw TomatoMallException.paymentFail("签名验证失败");
            }
            log.info("签名验证成功");

            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            log.info("交易状态: {}, 商户订单号: {}", tradeStatus, outTradeNo);

            switch (tradeStatus) {
                case "TRADE_SUCCESS":
                case "TRADE_FINISHED":
                    return handleSuccessfulPayment(params);
                case "TRADE_CLOSED":
                    return handleClosedPayment(outTradeNo);
                case "WAIT_BUYER_PAY":
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
     * 执行退款
     * 支持两种模式：
     * 1. order为null：退款整个支付，使用全部支付金额
     * 2. order不为null：退款指定订单，从对应商户账户扣款
     * @param payment 支付对象
     * @param order 订单对象（可以为空，为空则退款整个支付）
     * @param reason 退款原因
     */
    @Override
    @Transactional
    public void processRefund(Payment payment, Order order, String reason) {
        // 生成退款请求号：如果是订单退款则使用订单号，否则使用时间戳
        String refundRequestNo = "REFUND" + (order == null ? System.currentTimeMillis() : order.getOrderNo());
        // 确定退款金额：订单退款使用订单金额，整体退款使用支付金额
        BigDecimal refundAmount = order == null ? payment.getAmount() : order.getTotalAmount();
        // 获取商户账户：订单退款需要从对应商户账户扣款，整体退款则为null
        String merchantAccount = order == null ? null : order.getStore().getMerchantAccounts().get(PaymentMethod.ALIPAY);

        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent(buildRefundContent(payment, refundRequestNo, refundAmount, reason, merchantAccount).toString());

            if (config.getEncryptKey() != null && !config.getEncryptKey().isEmpty()) {
                request.setNeedEncrypt(true);
            }

            AlipayTradeRefundResponse response = alipayClient.certificateExecute(request);

            if (response.isSuccess()) {
                payment.setStatus(order == null ? PaymentStatus.PARTIALLY_REFUNDED : PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                eventPublisher.publishEvent(new RefundSuccessEvent(payment, order,
                        new BigDecimal(response.getRefundFee()), response.getTradeNo()));
            } else {
                // 退款失败，判断是否需要重试
                if (shouldRetry(response)) {
                    for (int i = 0; i < MAX_RETRY_COUNT; i++) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS * (i + 1));
                            AlipayTradeRefundRequest retryRequest = buildRetryRequest(payment, refundRequestNo, response);

                            if (config.getEncryptKey() != null && !config.getEncryptKey().isEmpty()) {
                                retryRequest.setNeedEncrypt(true);
                            }

                            AlipayTradeRefundResponse retryResponse = alipayClient.certificateExecute(retryRequest);
                            if (retryResponse.isSuccess()) {
                                payment.setStatus(order == null ? PaymentStatus.PARTIALLY_REFUNDED : PaymentStatus.REFUNDED);
                                paymentRepository.save(payment);
                                eventPublisher.publishEvent(new RefundSuccessEvent(payment, order,
                                        new BigDecimal(retryResponse.getRefundFee()), retryResponse.getTradeNo()));
                                return;
                            }
                        } catch (Exception e) {
                            log.error("退款重试失败", e);
                        }
                    }
                }
                // 所有重试都失败，发布退款失败事件
                eventPublisher.publishEvent(new RefundFailEvent(payment, order,
                        new BigDecimal(response.getRefundFee()), response.getTradeNo()));
                throw TomatoMallException.refundFail(response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("退款请求失败", e);
            throw TomatoMallException.refundFail(e.getMessage());
        }
    }

    /**
     * 处理支付超时
     * 查询交易状态，如果未成功则关闭交易并更新状态
     * @param payment 支付对象
     */
    @Override
    @Transactional
    public void processTimeout(Payment payment) {
        AlipayTradeQueryResponse response = queryTradeStatus(payment.getPaymentNo());
        if (!isTradeSuccess(response)) {
            closeTrade(payment);
            payment.setStatus(PaymentStatus.TIMEOUT);
            paymentRepository.save(payment);
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "支付超时"));
        }
    }

    /**
     * 查询支付宝交易状态
     * @param paymentNo 支付订单号
     * @return 支付宝交易查询响应
     */
    @Override
    @Transactional(readOnly = true)
    public AlipayTradeQueryResponse queryTradeStatus(String paymentNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent(new JSONObject().fluentPut("out_trade_no", paymentNo).toString());

            if (config.getEncryptKey() != null && !config.getEncryptKey().isEmpty()) {
                request.setNeedEncrypt(true);
            }

            return alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            throw TomatoMallException.operationFail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询支付宝退款状态
     * @param paymentNo 支付订单号
     * @param refundRequestNo 退款请求号
     * @return 支付宝退款查询响应
     */
    @Override
    @Transactional(readOnly = true)
    public AlipayTradeFastpayRefundQueryResponse queryRefundStatus(String paymentNo, String refundRequestNo) {
        try {
            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            JSONObject content = new JSONObject()
                    .fluentPut("out_trade_no", paymentNo)
                    .fluentPut("out_request_no", refundRequestNo);
            request.setBizContent(content.toString());

            if (config.getEncryptKey() != null && !config.getEncryptKey().isEmpty()) {
                request.setNeedEncrypt(true);
            }

            return alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            throw TomatoMallException.operationFail("查询失败：" + e.getMessage());
        }
    }

    // ====================================================================================
    // 私有方法
    // ====================================================================================

    /**
     * 处理支付成功的通知
     * 验证金额并更新支付状态，发布支付成功事件
     * @param params 支付宝通知参数
     * @return 处理结果
     */
    @Transactional
    public String handleSuccessfulPayment(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        Payment payment = paymentRepository.findByPaymentNo(outTradeNo)
                .orElseThrow(TomatoMallException::paymentNotFound);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return "success";
        }

        BigDecimal receivedAmount = new BigDecimal(params.get("total_amount"))
                .setScale(2, RoundingMode.HALF_UP);
        if (payment.getAmount().compareTo(receivedAmount) != 0) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            eventPublisher.publishEvent(new PaymentFailEvent(payment));
            return "fail";
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTradeNo(params.get("trade_no"));
        payment.setTransactionTime(LocalDateTime.now());
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentSuccessEvent(payment));
        return "success";
    }

    /**
     * 处理交易关闭的通知
     * 更新支付状态为失败，发布支付取消事件
     * @param outTradeNo 商户订单号
     * @return 处理结果
     */
    @Transactional
    public String handleClosedPayment(String outTradeNo) {
        paymentRepository.findByPaymentNo(outTradeNo).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            eventPublisher.publishEvent(new PaymentCancelEvent(payment, "交易关闭"));
        });
        return "success";
    }

    /**
     * 构建支付宝支付请求业务参数
     * 包含基本支付信息和分账配置
     * @param payment 支付对象
     * @return 业务参数JSON对象
     */
    private JSONObject buildPaymentBizContent(Payment payment) {
        JSONObject content = new JSONObject();
        content.put("out_trade_no", payment.getPaymentNo());
        content.put("total_amount", payment.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        content.put("subject", payment.getOrders() != null && !payment.getOrders().isEmpty()
                ? truncateSubject(payment.getOrders()) : payment.getEntityType());
        content.put("product_code", "FAST_INSTANT_TRADE_PAY");
        content.put("timeout_express", PAYMENT_TIMEOUT + "m");

        // 分账信息：为每个非系统商店的订单添加分账配置
        List<JSONObject> royaltyDetailInfos = new ArrayList<>();
        int serialNo = 1;
        for (Order order : payment.getOrders()) {
            if (!order.getStore().isSystemStore()) {
                String merchantAccount = order.getStore().getMerchantAccounts().get(PaymentMethod.ALIPAY);
                if (merchantAccount != null && !merchantAccount.trim().isEmpty()) {
                    JSONObject detail = new JSONObject();
                    detail.put("serial_no", String.valueOf(serialNo++));
                    detail.put("trans_in_type", "loginName");
                    detail.put("trans_in", merchantAccount);
                    detail.put("amount", order.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
                    detail.put("desc", "订单[" + order.getOrderNo() + "]商户分账");
                    royaltyDetailInfos.add(detail);
                }
            }
        }

        if (!royaltyDetailInfos.isEmpty()) {
            JSONObject royaltyInfo = new JSONObject();
            royaltyInfo.put("royalty_type", "ROYALTY");
            royaltyInfo.put("royalty_detail_infos", royaltyDetailInfos);
            content.put("royalty_info", royaltyInfo);
        }

        return content;
    }

    /**
     * 构建退款请求业务参数
     * 根据是否有商户账户决定是否进行分账退款
     * @param payment 支付对象
     * @param refundRequestNo 退款请求号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @param merchantAccount 商户账户（为空则普通退款，不为空则分账退款）
     * @return 业务参数JSON对象
     */
    private JSONObject buildRefundContent(Payment payment, String refundRequestNo, BigDecimal refundAmount, String reason, String merchantAccount) {
        JSONObject content = new JSONObject();
        content.put("out_trade_no", payment.getPaymentNo());
        content.put("trade_no", payment.getTradeNo());
        content.put("refund_amount", refundAmount.setScale(2, RoundingMode.HALF_UP));
        content.put("refund_reason", reason);
        content.put("out_request_no", refundRequestNo);

        // 如果指定了商户账户，从商户账户退款（分账退款）
        if (merchantAccount != null && !merchantAccount.trim().isEmpty()) {
            JSONObject royaltyParameters = new JSONObject();
            royaltyParameters.put("royalty_type", "ROYALTY");

            List<JSONObject> royaltyDetailInfos = new ArrayList<>();
            JSONObject detail = new JSONObject();
            detail.put("trans_out_type", "loginName");
            detail.put("trans_out", merchantAccount);  // 从商户账户退款
            detail.put("amount", refundAmount.setScale(2, RoundingMode.HALF_UP));

            royaltyDetailInfos.add(detail);
            royaltyParameters.put("royalty_detail_infos", royaltyDetailInfos);
            content.put("royalty_parameters", royaltyParameters);
        }

        return content;
    }

    /**
     * 构建退款重试请求
     * 用于退款失败时的重试机制
     * @param payment 支付对象
     * @param refundRequestNo 退款请求号
     * @param response 上一次退款响应
     * @return 退款请求对象
     */
    private AlipayTradeRefundRequest buildRetryRequest(Payment payment, String refundRequestNo, AlipayTradeRefundResponse response) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject content = new JSONObject();
        content.put("out_trade_no", payment.getPaymentNo());
        content.put("trade_no", payment.getTradeNo());
        content.put("refund_amount", response.getRefundFee());
        content.put("out_request_no", refundRequestNo);
        request.setBizContent(content.toString());
        return request;
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
        return subject.length() > 128 ? subject.substring(0, 125) + "..." : subject;
    }

    /**
     * 转换HTTP请求参数为Map
     * @param request HTTP请求对象
     * @return 参数Map
     */
    private Map<String, String> convertParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            String value = String.join(",", values);
            params.put(key, value);
        });
        log.debug("收到支付宝回调参数: {}", params);
        return params;
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

    /**
     * 判断是否为分账相关错误
     * @param subCode 支付宝错误子码
     * @return 是否为分账错误
     */
    private boolean isSplitAccountError(String subCode) {
        return subCode != null && (
                subCode.contains("ROYALTY") ||
                        subCode.contains("PAYEE_NOT_EXIST") ||
                        subCode.contains("PAYEE_USER_INFO_ERROR") ||
                        subCode.contains("SPLIT_BILL_FAIL") ||
                        subCode.equals("INVALID_PARAMETER.TRANS_IN")
        );
    }

    /**
     * 判断是否应该重试退款
     * 仅在系统错误或特定业务错误时重试
     * @param response 退款响应
     * @return 是否应该重试
     */
    private boolean shouldRetry(AlipayTradeRefundResponse response) {
        String code = response.getSubCode();
        return code != null && (code.startsWith("ACQ.") || code.equals("SYSTEM_ERROR"));
    }
}