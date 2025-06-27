package cn.edu.nju.TomatoMall.service.impl.events.payment;

import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;

@Getter
public class PaymentSuccessEvent extends PaymentEvent {
    public PaymentSuccessEvent(Payment payment) {
        super(payment);
    }
}
