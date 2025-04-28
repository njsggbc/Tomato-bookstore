package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.models.po.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.NonNull;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@NonNull
public class OrderItemInfoResponse {
    private int id;
    private int productId;
    private String productName;
    private String cover;
    private BigDecimal price;
    private int quantity;
    private BigDecimal totalPrice;
    private int snapshotId;

    public OrderItemInfoResponse(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.productId = orderItem.getProductId();
        this.productName = orderItem.getProductSnapshot().getName();
        this.cover = orderItem.getProductSnapshot().getImages().get(0);
        this.price = orderItem.getProductSnapshot().getPrice();
        this.quantity = orderItem.getQuantity();
        this.totalPrice = orderItem.getTotalPrice();
        this.snapshotId = orderItem.getProductSnapshot().getId();
    }
}
