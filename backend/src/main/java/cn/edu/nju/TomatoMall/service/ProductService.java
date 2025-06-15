package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.product.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品管理服务接口
 */
public interface ProductService {
    /**
     * 创建商品
     * @param storeId 商店ID
     * @param title 商品标题
     * @param description 商品描述
     * @param images 商品图片
     * @param price 商品价格
     * @param specifications 商品规格
     */
    void createProduct(int storeId, String title, String description, List<MultipartFile> images, BigDecimal price, Map<String,String> specifications);

    /**
     * 更新商品信息
     * @param productId 商品ID
     * @param title 商品标题
     * @param description 商品描述
     * @param images 商品图片
     * @param price 商品价格
     * @param specifications 商品规格
     */
    void updateProduct(int productId, String title, String description, List<MultipartFile> images, BigDecimal price, Map<String,String> specifications);

    /**
     * 删除商品
     * @param productId 商品ID
     * @return 操作结果信息
     */
    String deleteProduct(int productId);

    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品详细信息
     */
    ProductDetailResponse getProductDetail(int productId);

    /**
     * 获取商品列表
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @return 商品简要信息分页
     */
    Page<ProductBriefResponse> getProductList(int page, int size, String field, boolean order);

    /**
     * 获取商店商品列表
     * @param storeId 商店ID
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @return 商品简要信息分页
     */
    Page<ProductBriefResponse> getStoreProductList(int storeId, int page, int size, String field, boolean order);

    /**
     * 获取商品快照信息
     * @param snapshotId 快照ID
     * @return 快照信息
     */
    ProductSnapshotResponse getSnapshot(int snapshotId);

    /**
     * 调整商品库存
     * @param productId 商品ID
     * @param stockpile 库存量
     * @return 操作结果信息
     */
    String updateStockpile(int productId, int stockpile);

    /**
     * 调整商品库存预警值
     * @param productId 商品ID
     * @param threshold 预警阈值
     */
    void updateThreshold(int productId, int threshold);

    /**
     * 获取商品库存信息
     * @param productId 商品ID
     * @return 库存数量
     */
    ProductInventoryResponse getStockpile(int productId);
}
