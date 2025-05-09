package cn.edu.nju.TomatoMall.events.payment;

import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;

@Getter
public class PaymentCancelEvent extends PaymentEvent{
    private final String reason;

    public PaymentCancelEvent(Payment payment, String reason) {
        super(payment);
        this.reason = reason;
    }
}
