package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * search
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 搜索商品
     * @param keyword
     * @param page
     * @param size
     * @param field
     * @param order
     * @param minPrice
     * @param maxPrice
     * @return 商品简略信息分页响应
     */
    @GetMapping("/products")
    public ApiResponse<Page<ProductBriefResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) Boolean order,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ApiResponse.success(
                searchService.searchProducts(
                        keyword, page < 0 ? 0 : page, size < 0 ? 10 : size, field, order, minPrice, maxPrice
                )
        );
    }

    /**
     * 搜索商店
     * @param keyword
     * @param page
     * @param size
     * @param field
     * @param order
     * @return 商店信息分页响应
     */
    @GetMapping("/stores")
    public ApiResponse<Page<StoreInfoResponse>> searchStores(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) Boolean order
    ) {
        return ApiResponse.success(
                searchService.searchStores(
                        keyword, page < 0 ? 0 : page, size < 0 ? 10 : size, field, order
                )
        );
    }
}
