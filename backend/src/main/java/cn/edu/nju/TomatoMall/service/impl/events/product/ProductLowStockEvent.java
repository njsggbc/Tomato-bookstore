package cn.edu.nju.TomatoMall.service.impl.events.product;

import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Getter;

@Getter
public class ProductLowStockEvent extends ProductEvent {
    private final int stock;

    public ProductLowStockEvent(Product product, int stock) {
        super(product);
        this.stock = stock;
    }
}
