package cn.edu.nju.TomatoMall.events.payment;

import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentSuccessEvent extends PaymentEvent {
    public PaymentSuccessEvent(Payment payment) {
        super(payment);
    }
}
