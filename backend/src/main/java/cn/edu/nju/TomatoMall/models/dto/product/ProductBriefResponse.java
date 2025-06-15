package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class ProductBriefResponse {
    int id;
    String title;
    String description;
    String cover;
    BigDecimal price;
    BigDecimal rate;
    Integer sales;
    InventoryStatus inventoryStatus;

    public ProductBriefResponse(Product product) {
        this.id = product.getId();
        this.title = product.getName();
        this.description = product.getDescription();
        this.cover = product.getImages().get(0);
        this.price = product.getPrice();
        this.rate = product.getRating();
        this.sales = product.getSales();
        this.inventoryStatus = product.getInventoryStatus();
    }
}
