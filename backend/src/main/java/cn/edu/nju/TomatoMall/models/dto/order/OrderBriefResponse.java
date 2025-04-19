package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@NonNull
public class OrderBriefResponse {
    private int orderId;
    private String orderNo;
    private OrderStatus status;
    private List<OrderItemInfoResponse> items;
    private BigDecimal totalAmount;
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
        this.totalAmount = order.getTotalAmount();
        this.createTime = order.getCreateTime().toString();
        this.storeId = order.getStore().getId();
        this.storeName = order.getStore().getName();
    }
}
