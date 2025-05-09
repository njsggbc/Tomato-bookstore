package cn.edu.nju.TomatoMall.events.order;

import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;

@Getter
public class OrderEvent {
    private final Order order;

    public OrderEvent(Order order) {
        this.order = order;
    }
}
