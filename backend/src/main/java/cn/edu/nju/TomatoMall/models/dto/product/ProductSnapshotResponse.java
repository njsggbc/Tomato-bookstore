package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.models.po.ProductSnapshot;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ProductSnapshotResponse {
    private int id;
    private int productId;
    private String productName;
    private String description;
    private List<String> images;
    private BigDecimal price;
    private Map<String, String> specifications;
    private LocalDateTime createTime;

    public ProductSnapshotResponse(ProductSnapshot productSnapshot) {
        this.id = productSnapshot.getId();
        this.productId = productSnapshot.getProduct().getId();
        this.productName = productSnapshot.getName();
        this.description = productSnapshot.getDescription();
        this.images = productSnapshot.getImages();
        this.price = productSnapshot.getPrice();
        this.specifications = productSnapshot.getSpecifications();
        this.createTime = productSnapshot.getCreateTime();
    }
}
