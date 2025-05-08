package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductDetailResponse {

    int id;
    String title;
    String description;
    String cover;
    List<String> images;
    BigDecimal price;
    Double rate;
    Map<String, String> specifications;
    int storeId;
    String createTime;
    Integer sales;
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
