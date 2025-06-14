package cn.edu.nju.TomatoMall.service.impl.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;

public class OrderConfirmEvent extends OrderEvent {
    public OrderConfirmEvent(Order order) {
        super(order);
    }
}
