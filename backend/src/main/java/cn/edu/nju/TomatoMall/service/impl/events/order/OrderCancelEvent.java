package cn.edu.nju.TomatoMall.service.impl.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderCancelEvent extends OrderEvent{
    private final BigDecimal refundAmount;
    private final String reason;

    public OrderCancelEvent(Order order, BigDecimal refundAmount, String reason) {
        super(order);
        this.refundAmount = refundAmount;
        this.reason = reason;
    }
}
