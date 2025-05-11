package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 支付管理服务接口
 */
public interface PaymentService {
    /**
     * 发起支付
     * @param paymentId 支付ID
     * @param paymentMethod 支付方式
     * @return 支付结果信息
     */
    String pay(int paymentId, PaymentMethod paymentMethod);

    /**
     * 取消支付
     * @param paymentId 支付ID
     */
    void cancel(int paymentId);

    /**
     * 处理支付通知回调
     * @param request HTTP请求
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    String handlePaymentNotify(HttpServletRequest request, PaymentMethod paymentMethod);

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