package cn.edu.nju.TomatoMall.events.payment;

import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;

@Getter
public abstract class PaymentEvent {
    private final Payment payment;

    public PaymentEvent(Payment payment) {
        this.payment = payment;
    }
}