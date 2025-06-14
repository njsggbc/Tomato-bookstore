package cn.edu.nju.TomatoMall.service.impl.events.payment;

import cn.edu.nju.TomatoMall.models.po.Payment;

public class PaymentFailEvent extends PaymentEvent {
    public PaymentFailEvent(Payment payment) {
        super(payment);
    }
}
