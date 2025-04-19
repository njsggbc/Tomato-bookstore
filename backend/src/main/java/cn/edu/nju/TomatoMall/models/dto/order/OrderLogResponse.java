package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.OrderEvent;
import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.models.po.OrderLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderLogResponse {
    private int id;
    private OrderEvent event;
    private OrderStatus afterEventStatus;
    private String message;
    private String timestamp;

    public OrderLogResponse(OrderLog orderLog) {
        this.id = orderLog.getId();
        this.event = orderLog.getEvent();
        this.afterEventStatus = orderLog.getAfterEventStatus();
        this.message = orderLog.getMessage();
        this.timestamp = orderLog.getTimestamp().toString();
    }
}
