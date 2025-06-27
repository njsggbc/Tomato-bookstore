package cn.edu.nju.TomatoMall.service.impl.strategy;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;

import javax.servlet.http.HttpServletRequest;


public interface PaymentStrategy {
    /**
     * 获取支付方式
     * @return 支付方式枚举
     */
    PaymentMethod getPaymentMethod();

    /**
     * 创建支付交易
     * @param payment 支付信息
     * @return 支付跳转链接
     */
    String createTrade(Payment payment);

    /**
     * 关闭支付交易
     * @param payment 支付信息
     */
    void closeTrade(Payment payment);

    /**
     * 处理支付通知回调
     * @param request HTTP请求
     * @return 处理结果
     */
    Object processPaymentNotify(HttpServletRequest request);

    /**
     * 处理退款
     * @param payment
     * @param order
     * @param reason
     */
    void processRefund(Payment payment, Order order, String reason);

    /**
     * 处理支付超时
     * @param payment 支付信息
     */
    void processTimeout(Payment payment);

    /**
     * 查询交易状态
     * @param paymentNo 支付单号
     * @return 交易状态信息
     */
    Object queryTradeStatus(String paymentNo);

    /**
     * 查询退款状态
     * @param paymentNo 支付单号
     * @param orderNo 订单编号
     * @return 退款状态信息
     */
    Object queryRefundStatus(String paymentNo, String orderNo);
}
