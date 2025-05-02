package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Service
public interface PaymentService {
    String pay(String paymentId, PaymentMethod paymentMethod);
    void cancel(String paymentId);
    String handlePaymentNotify(HttpServletRequest request, PaymentMethod paymentMethod);
    Object queryTradeStatus(String paymentId);
    Object queryRefundStatus(String paymentId, String orderNo);
}