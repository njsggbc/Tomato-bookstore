package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailResponse {

    int id;
    String title;
    String description;
    String cover;
    List<String> images;
    BigDecimal price;
    double rate;
    Map<String, String> specifications;
    int storeId;
    String createTime;
    int sales;
    InventoryStatus inventoryStatus;

    public ProductDetailResponse(Product product) {
        this.id = product.getId();
        this.title = product.getName();
        this.description = product.getDescription();
        this.images = product.getImages();
        this.cover = images.get(0);
        this.price = product.getPrice();
        this.rate = product.getRate();
        this.specifications = product.getSpecifications();
        this.storeId = product.getStore().getId();
        this.sales = product.getSales();
        this.inventoryStatus = product.getInventoryStatus();
        this.createTime = product.getCreateTime().toString();
    }
}
