package cn.edu.nju.TomatoMall.models.dto.payment;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.dto.order.OrderBriefResponse;
import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PaymentInfoResponse {
    private int paymentId;
    private List<OrderBriefResponse> orderList;
    private BigDecimal totalAmount;

    private PaymentMethod paymentMethod;
    private String paymentNo;
    private String tradeNo;


    public PaymentInfoResponse(Payment payment) {
        this.paymentId = payment.getId();
        this.orderList = payment.getOrders().stream()
                .map(OrderBriefResponse::new)
                .collect(Collectors.toList());
        this.totalAmount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentNo = payment.getPaymentNo();
        this.tradeNo = payment.getTradeNo();
    }
}
