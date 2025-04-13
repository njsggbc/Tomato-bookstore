package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.product.ProductCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.product.ProductDetailResponse;
import cn.edu.nju.TomatoMall.models.dto.product.ProductUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface ProductService {
    void createProduct(ProductCreateRequest params);

    void updateProduct(int productId, ProductUpdateRequest params);

    // HACK: 为了测试要返回毫无意义的字符串 😩
    String deleteProduct(int productId);

    ProductDetailResponse getProductDetail(int productId);

    Page<ProductBriefResponse> getProductList(int page, int size, String field, boolean order);

    Page<ProductBriefResponse> getStoreProductList(int storeId, int page, int size, String field, boolean order);

    /*---------- HACK: 以下为兼容测试用接口 ----------*/

    ProductBriefResponse createProduct(Map<String, Object> params);

    String updateProduct(Map<String, Object> params);

    String updateStockpile(int productId, int stockpile);

    int getStockpile(int productId);

}
