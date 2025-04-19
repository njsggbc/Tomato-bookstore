package cn.edu.nju.TomatoMall.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderRefundEvent extends OrderEvent{
    private final BigDecimal refundAmount;
    private final String reason;

    public OrderRefundEvent(Order order, BigDecimal refundAmount,String reason) {
        super(order);
        this.refundAmount = refundAmount;
        this.reason = reason;
    }
}
