package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderBriefResponse {
    private int orderId;
    private String orderNo;
    private OrderStatus status;
    private List<OrderItemInfoResponse> items;
    private BigDecimal totalPrice;
    private String createTime;
    private int storeId;
    private String storeName;

    public OrderBriefResponse(Order order) {
        this.orderId = order.getId();
        this.orderNo = order.getOrderNo();
        this.status = order.getStatus();
        this.items = order.getItems().stream()
                .map(OrderItemInfoResponse::new)
                .collect(Collectors.toList());
        this.totalPrice = order.getTotalAmount();
        this.createTime = order.getCreateTime().toString();
        this.storeId = order.getStore().getId();
        this.storeName = order.getStore().getName();
    }
}
