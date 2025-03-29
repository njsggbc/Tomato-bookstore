package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductBriefResponse {
    int id;
    String title;
    String description;
    String cover;
    double price;
    int sales;
    double rate;

    public ProductBriefResponse(Product product) {
        this.id = product.getId();
        this.title = product.getName();
        this.description = product.getDescription();
        this.cover = product.getImages().get(0);
        this.price = product.getPrice();
        this.sales = product.getSales();
        this.rate = product.getRate();
    }
}
