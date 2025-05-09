package cn.edu.nju.TomatoMall.enums;

public enum InventoryStatus {
    SUFFICIENT,
    INSUFFICIENT,
    OUT_OF_STOCK;

    public static InventoryStatus getInventoryStatus(int stock, int inventoryThreshold) {
        if (stock > inventoryThreshold) {
            return SUFFICIENT;
        } else if (stock == 0) {
            return OUT_OF_STOCK;
        } else {
            return INSUFFICIENT;
        }
    }
}
