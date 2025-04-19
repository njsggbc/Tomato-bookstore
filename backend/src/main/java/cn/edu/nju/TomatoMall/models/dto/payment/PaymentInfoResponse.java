package cn.edu.nju.TomatoMall.models.dto.payment;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.dto.order.OrderBriefResponse;
import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class PaymentInfoResponse {
    @NonNull
    private String paymentId;
    @NonNull
    private List<OrderBriefResponse> orderList;
    @NonNull
    private BigDecimal totalAmount;

    private PaymentMethod paymentMethod;
    private String tradeNo;


    public PaymentInfoResponse(Payment payment) {
        this.paymentId = payment.getId();
        this.orderList = payment.getOrders().stream()
                .map(OrderBriefResponse::new)
                .collect(Collectors.toList());
        this.totalAmount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.tradeNo = payment.getTradeNo();
    }
}
