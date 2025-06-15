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
    private int id;
    private String title;
    private String cover;
    private BigDecimal price;
    private Double rate;
    private Integer sales;
    private InventoryStatus inventoryStatus;

    public ProductBriefResponse(Product product) {
        this.id = product.getId();
        this.title = product.getName();
        this.cover = product.getImages().get(0);
        this.price = product.getPrice();
        this.rate = product.getRate();
        this.sales = product.getSales();
        this.inventoryStatus = product.getInventoryStatus();
    }
}
