package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.po.CartItem;
import lombok.Data;

@Data
public class CartItemInfoResponse {
    private int id;
    private ProductBriefResponse product;
    private int quantity;

    public CartItemInfoResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.quantity = cartItem.getQuantity();
        this.product = new ProductBriefResponse(cartItem.getProduct());
    }
}
