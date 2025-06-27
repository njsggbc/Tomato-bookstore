package cn.edu.nju.TomatoMall.service.impl.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

@Getter
public class OrderShipEvent extends OrderEvent {
    private final String trackingNumber;
    private final String carrier;

    public OrderShipEvent(Order order, String trackingNumber, String carrier) {
        super(order);
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
    }
}
