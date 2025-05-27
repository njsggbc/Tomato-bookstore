package cn.edu.nju.TomatoMall.events.product;

import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Getter;

@Getter
public abstract class ProductEvent {
    private final Product product;

    public ProductEvent(Product product) {
        this.product = product;
    }
}
