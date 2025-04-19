package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.po.Inventory;
import cn.edu.nju.TomatoMall.models.po.Product;

public interface InventoryService {

    /**
     * 初始化商品库存
     * @param product 商品
     * @param initialQuantity 初始库存数量
     */
    Inventory initializeInventory(Product product, int initialQuantity);

    /**
     * 设置库存 - 用于初始化或重置库存
     * @param productId 商品ID
     * @param quantity 设置数量
     */
    void setStock(int productId, int quantity);

    /**
     * 锁定库存 - 用于下单时锁定库存，但不实际减少
     * @param productId 商品ID
     * @param quantity 锁定数量
     */
    void lockStock(int productId, int quantity);

    /**
     * 解锁库存 - 用于取消订单时释放锁定的库存
     * @param productId 商品ID
     * @param quantity 解锁数量
     */
    void unlockStock(int productId, int quantity);

    /**
     * 确认库存扣减 - 用于订单支付后，将锁定的库存真正扣减
     * @param productId 商品ID
     * @param quantity 确认扣减数量
     */
    void confirmStockDeduction(int productId, int quantity);

    /**
     * 获取可用库存
     * @param productId 商品ID
     * @return 可用库存数量
     */
    int getAvailableStock(int productId);

    /**
     * 检查库存是否充足
     * @param productId 商品ID
     * @param quantity 请求数量
     * @return 是否充足
     */
    boolean checkStock(int productId, int quantity);

//    TODO: 库存预警
//    boolean setStockThreshold(int productId, int threshold);
}