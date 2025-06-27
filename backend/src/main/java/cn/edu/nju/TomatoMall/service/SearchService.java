package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface SearchService {
    /**
     * 搜索商品
     *
     * @param keyword    搜索关键词
     * @param page       页码
     * @param size       每页大小
     * @param field      排序字段
     * @param order      排序方式，true为升序，false为降序
     * @param minPrice   最小价格（可选）
     * @param maxPrice   最大价格（可选）
     * @return 商品简要信息分页
     */
    Page<ProductBriefResponse> searchProducts(
            String keyword,
            int page,
            int size,
            String field,
            Boolean order,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    /**
     * 搜索商店
     *
     * @param keyword 搜索关键词
     * @param page    页码
     * @param size    每页大小
     * @param field   排序字段
     * @param order   排序方式，true为升序，false为降序
     * @return 商店信息分页
     */
    Page<StoreInfoResponse> searchStores(
            String keyword,
            int page,
            int size,
            String field,
            Boolean order
    );
}
