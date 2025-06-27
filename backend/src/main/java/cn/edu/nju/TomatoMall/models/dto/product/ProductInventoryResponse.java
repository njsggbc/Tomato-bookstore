package cn.edu.nju.TomatoMall.models.dto.product;

import cn.edu.nju.TomatoMall.models.po.Inventory;
import lombok.Data;

@Data
public class ProductInventoryResponse {
    private final int stock;
    private final int reserved;
    private final int threshold;

    public ProductInventoryResponse(Inventory inventory) {
        this.stock = inventory.getQuantity();
        this.reserved = inventory.getLockedQuantity();
        this.threshold = inventory.getThresholdQuantity();
    }
}
