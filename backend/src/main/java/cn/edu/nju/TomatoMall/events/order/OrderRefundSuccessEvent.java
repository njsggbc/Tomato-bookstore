package cn.edu.nju.TomatoMall.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderRefundSuccessEvent extends OrderEvent{
    private final BigDecimal refundAmount;
    private final String tradeNo;
    public OrderRefundSuccessEvent(Order order, BigDecimal refundAmount, String tradeNo) {
        super(order);
        this.refundAmount = refundAmount;
        this.tradeNo = tradeNo;
    }
}
