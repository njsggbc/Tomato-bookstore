package cn.edu.nju.TomatoMall.events.payment;

import cn.edu.nju.TomatoMall.events.order.OrderEvent;
import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RefundSuccessEvent extends PaymentEvent {
    private final Order order;
    private final BigDecimal refundAmount;
    private final String tradeNo;
    public RefundSuccessEvent(Order order, BigDecimal refundAmount, String tradeNo) {
        super(null);
        this.order = order;
        this.refundAmount = refundAmount;
        this.tradeNo = tradeNo;
    }
}
