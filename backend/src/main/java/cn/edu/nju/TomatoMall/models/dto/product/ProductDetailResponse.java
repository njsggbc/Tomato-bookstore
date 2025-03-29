package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.models.po.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    double price;
    int stock;
    double rate;
    int sales;
    Map<String, String> specifications;
    int storeId;
    String createTime;

    public ProductDetailResponse(Product product) {
        this.id = product.getId();
        this.title = product.getName();
        this.description = product.getDescription();
        this.images = product.getImages();
        this.cover = images.get(0);
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.rate = product.getRate();
        this.sales = product.getSales();
        this.specifications = product.getSpecifications();
        this.storeId = product.getStore().getId();
        this.createTime = product.getCreateTime().toString();
    }
}
