package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
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
    private ProductBriefResponse product;
    private int quantity;
    private BigDecimal price;

    public OrderItemInfoResponse(OrderItem cartItem) {
        this.id = cartItem.getId();
        this.product = new ProductBriefResponse(cartItem.getProduct());
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getUnitPriceSnapshot();
    }
}
