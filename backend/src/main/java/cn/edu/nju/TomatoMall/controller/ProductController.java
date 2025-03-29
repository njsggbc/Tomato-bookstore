package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.product.ProductCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.product.ProductDetailResponse;
import cn.edu.nju.TomatoMall.models.dto.product.ProductUpdateRequest;
import cn.edu.nju.TomatoMall.service.ProductService;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     */
    @GetMapping("/all")
    public ApiResponse<Page<ProductBriefResponse>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "id") String field,
            @RequestParam(defaultValue = "true") boolean order) {

        Page<ProductBriefResponse> productPage = productService.getProductList(page, size, field, order);
        return ApiResponse.success(productPage);
    }

    /**
     * 创建新商品
     */
    @PostMapping(path = "/create", consumes = "multipart/form-data")
    public ApiResponse<Boolean> createProduct(@ModelAttribute ProductCreateRequest params) {
        return ApiResponse.success(productService.createProduct(params));
    }

    /**
     * 获取商品信息
     */
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProductInfo(@PathVariable int productId) {
        return ApiResponse.success(productService.getProductDetail(productId));
    }

    /**
     * 更新商品信息
     */
    @PatchMapping(path = "/{productId}", consumes = "multipart/form-data")
    public ApiResponse<Boolean> updateProduct(@PathVariable int productId, @ModelAttribute ProductUpdateRequest params) {
        return ApiResponse.success(productService.updateProduct(productId, params));
    }

    /**
     * 删除商品, HACK: 返回一个毫无意义的字符串
     */
    @DeleteMapping("/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable int productId) {
        return ApiResponse.success(productService.deleteProduct(productId));
    }

    /**
     * 调整商品库存
     */
    @PatchMapping("/stockpile/{productId}")
    public ApiResponse<String> updateStockpile(@PathVariable int productId, @RequestBody Map<String, Integer> params) {
        return ApiResponse.success(productService.updateStockpile(productId, params.get("amount")));
    }

    /**
     * 获取商品库存信息
     */
    @GetMapping("/stockpile/{productId}")
    public ApiResponse<Integer> getStockpile(@PathVariable int productId) {
        return ApiResponse.success(productService.getStockpile(productId));
    }

    /*------ HACK: 以下为兼容测试用接口 ------*/

    /**
     * 兼容测试用：更新商品信息
     */
    @PutMapping
    public ApiResponse<String> updateProduct(@RequestBody Map<String, Object> params) {
        return ApiResponse.success(productService.updateProduct(params));
    }

    /**
     * 兼容测试用：创建商品
     */
    @PostMapping
    public ApiResponse<ProductBriefResponse> createProduct(@RequestBody Map<String, Object> params) {
        return ApiResponse.success(productService.createProduct(params));
    }

    /**
     * 兼容测试用：获取商品列表
     */
    @GetMapping
    public ApiResponse<List<ProductBriefResponse>> getProductList() {
        return ApiResponse.success(productService.getProductList(0, 0, "id", true).getContent());
    }
}