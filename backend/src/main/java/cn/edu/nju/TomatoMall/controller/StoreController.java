package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.store.StoreCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.store.StoreBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreDetailResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreUpdateRequest;
import cn.edu.nju.TomatoMall.service.StoreService;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * 获取商店列表
     */

    @GetMapping
    public ApiResponse<Page<StoreBriefResponse>> getStoreList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "id") String field,
            @RequestParam(defaultValue = "true") boolean order) {
        Page<StoreBriefResponse> storePage = storeService.getStoreList(page, size, field, order);
        return ApiResponse.success(storePage);
    }

    /**
     * 创建新商店
     */
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<Boolean> createStore(@ModelAttribute StoreCreateRequest params) {
        return ApiResponse.success(storeService.createStore(params));
    }

    /**
     * 获取商店详细信息
     */
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> getStore(@PathVariable int storeId) {
        return ApiResponse.success(storeService.getDetail(storeId));
    }

    /**
     * 获取商店简略信息
     */
    @GetMapping("/{storeId}/brief")
    public ApiResponse<StoreBriefResponse> getStoreInfo(@PathVariable int storeId) {
        return ApiResponse.success(storeService.getBrief(storeId));
    }

    /**
     * 更新商店信息
     */
    @PatchMapping(path = "/{storeId}", consumes = "multipart/form-data")
    public ApiResponse<Boolean> updateStore(@PathVariable int storeId, @ModelAttribute StoreUpdateRequest params) {
        return ApiResponse.success(storeService.updateStore(storeId, params));
    }

    /**
     * 删除商店
     */
    @DeleteMapping("/{storeId}")
    public ApiResponse<Boolean> deleteStore(@PathVariable int storeId) {
        return ApiResponse.success(storeService.deleteStore(storeId));
    }

    /**
     * 获取商店的令牌列表
     */
    @GetMapping("/{storeId}/tokens")
    public ApiResponse<List<String>> getStoreTokenList(@PathVariable int storeId) {
        return ApiResponse.success(storeService.getStoreTokenList(storeId));
    }

    /**
     * 生成商店令牌
     */
    @PostMapping("/{storeId}/tokens")
    public ApiResponse<Boolean> generateToken(@PathVariable int storeId) {
        return ApiResponse.success(storeService.generateToken(storeId));
    }

    /**
     * 删除商店令牌
     */
    @DeleteMapping("/{storeId}/tokens")
    public ApiResponse<Boolean> deleteToken(@PathVariable int storeId, @RequestParam String token) {
        return ApiResponse.success(storeService.deleteToken(storeId, token));
    }

    /**
     * 验证商店令牌
     */
    @PostMapping("/{storeId}/staffs")
    public ApiResponse<Boolean> authToken(@PathVariable int storeId, @RequestParam String token) {
        return ApiResponse.success(storeService.authToken(storeId, token));
    }

    /**
     * 删除商店员工
     */
    @DeleteMapping("/{storeId}/staffs")
    public ApiResponse<Boolean> deleteStaff(@PathVariable int storeId, @RequestParam int staffId) {
        return ApiResponse.success(storeService.deleteStaff(storeId, staffId));
    }

    /**
     * 审核店铺（管理员权限）
     *
     * @param storeId 店铺ID
     * @param pass    是否审核通过
     * @return 操作成功与否
     */
    @PostMapping("/review")
    public ApiResponse<Boolean> reviewStore(
            @RequestParam int storeId,
            @RequestParam boolean pass) {

        return ApiResponse.success(storeService.review(storeId, pass));
    }

    /**
     * 获取待审核的店铺列表（管理员权限）
     *
     * @param page      页码
     * @param size      每页大小, 0表示不分页
     * @param field 排序字段
     * @param order     排序方式（true: 升序, false: 降序）
     * @return 店铺分页列表
     */
    @GetMapping("/awaiting-review")
    public ApiResponse<Page<StoreBriefResponse>> getAwaitingReviewStoreList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "id") String field,
            @RequestParam(defaultValue = "true") boolean order) {

        return ApiResponse.success(storeService.getAwaitingReviewStoreList(page, size, field, order));
    }

    /**
     * 获取被暂停的店铺列表（管理员权限）
     *
     * @param page      页码
     * @param size      每页大小, 0表示不分页
     * @param field 排序字段
     * @param order     排序方式（true: 升序, false: 降序）
     * @return 店铺分页列表
     */
    @GetMapping("/suspended")
    public ApiResponse<Page<StoreBriefResponse>> getSuspendedStoreList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "id") String field,
            @RequestParam(defaultValue = "true") boolean order) {

        return ApiResponse.success(storeService.getSuspendedStoreList(page, size, field, order));
    }

    /**
     * 获取店铺的资质信息（管理员权限）
     *
     * @param storeId 店铺ID
     * @return 资质信息列表
     */
    @GetMapping("/{storeId}/qualifications")
    public ApiResponse<List<String>> getStoreQualifications(@PathVariable int storeId) {
        return ApiResponse.success(storeService.getStoreQualification(storeId));
    }
}