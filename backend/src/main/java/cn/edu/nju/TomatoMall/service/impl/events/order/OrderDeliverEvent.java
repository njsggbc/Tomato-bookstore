package cn.edu.nju.TomatoMall.service.impl.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

@Getter
public class OrderDeliverEvent extends OrderEvent {
    private final String deliveryTime;
    private final String deliveryAddress;

    public OrderDeliverEvent(Order order, String deliveryTime, String deliveryAddress) {
        super(order);
        this.deliveryTime = deliveryTime;
        this.deliveryAddress = deliveryAddress;
    }
}
