package cn.edu.nju.TomatoMall.service.impl.events.payment;

import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;

@Getter
public abstract class PaymentEvent {
    private final Payment payment;

    public PaymentEvent(Payment payment) {
        this.payment = payment;
    }
}