package cn.edu.nju.TomatoMall.events.payment;

import cn.edu.nju.TomatoMall.models.po.Payment;

public class PaymentCreateEvent extends PaymentEvent {
    public PaymentCreateEvent(Payment payment) {
        super(payment);
    }
}
