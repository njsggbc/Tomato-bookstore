package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.product.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {
    void createProduct(int storeId, String title, String description, List<MultipartFile> images, BigDecimal price, Map<String,String> specifications);

    void updateProduct(int productId, String title, String description, List<MultipartFile> images, BigDecimal price, Map<String,String> specifications);

    // HACK: 为了测试要返回毫无意义的字符串 😩
    String deleteProduct(int productId);

    ProductDetailResponse getProductDetail(int productId);

    Page<ProductBriefResponse> getProductList(int page, int size, String field, boolean order);

    Page<ProductBriefResponse> getStoreProductList(int storeId, int page, int size, String field, boolean order);

    ProductSnapshotResponse getSnapshot(int snapshotId);

    String updateStockpile(int productId, int stockpile);

    void updateThreshold(int productId, int threshold);

    int getStockpile(int productId);

    /*---------- HACK: 以下为兼容测试用接口 ----------*/

    ProductBriefResponse createProduct(Map<String, Object> params);

    String updateProduct(Map<String, Object> params);
}
