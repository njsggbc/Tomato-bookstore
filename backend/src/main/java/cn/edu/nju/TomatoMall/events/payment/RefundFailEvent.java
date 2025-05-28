package cn.edu.nju.TomatoMall.events.payment;

import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.models.po.Payment;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RefundFailEvent extends PaymentEvent {
    private final Order order;
    private final BigDecimal refundAmount;
    private final String tradeNo;

    public RefundFailEvent(Order order, BigDecimal refundAmount, String tradeNo) {
        super(null);
        this.order = order;
        this.refundAmount = refundAmount;
        this.tradeNo = tradeNo;
    }
}
