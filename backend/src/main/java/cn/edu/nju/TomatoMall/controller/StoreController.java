package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.dto.employment.TokenGenerateRequest;
import cn.edu.nju.TomatoMall.models.dto.employment.TokenInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreCreateRequest;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.store.StoreUpdateRequest;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.EmploymentService;
import cn.edu.nju.TomatoMall.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * store
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {


    @Autowired
    private StoreService storeService;

    @Autowired
    private EmploymentService employmentService;

    /**
     * 获取商店列表
     */
    @GetMapping
    public ApiResponse<Page<StoreInfoResponse>> getStoreList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "id") String field,
            @RequestParam(defaultValue = "true") boolean order) {
        Page<StoreInfoResponse> storePage = storeService.getStoreList(page, size, field, order);
        return ApiResponse.success(storePage);
    }

    /**
     * 获取当前用户管理的商店列表
     */
    @GetMapping("/managed")
    public ApiResponse<List<StoreInfoResponse>> getManagedStoreList() {
        List<StoreInfoResponse> storeList = storeService.getManagedStoreList();
        return ApiResponse.success(storeList);
    }

    /**
     * 获取当前用户工作的商店列表
     */
    @GetMapping("/worked")
    public ApiResponse<List<StoreInfoResponse>> getWorkedStoreList() {
        List<StoreInfoResponse> storeList = storeService.getWorkedStoreList();
        return ApiResponse.success(storeList);
    }

    /**
     * 创建新商店
     */
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<Void> createStore(
            @Valid @ModelAttribute StoreCreateRequest params
    ) {
        storeService.createStore(
                params.getName(),
                params.getDescription(),
                params.getLogo(),
                params.getAddress(),
                params.getQualifications(),
                params.getMerchantAccounts()
        );
        return ApiResponse.success();
    }

    /**
     * 获取商店信息
     */
    @GetMapping("/{storeId}")
    public ApiResponse<StoreInfoResponse> getStore(@PathVariable int storeId) {
        return ApiResponse.success(storeService.getInfo(storeId));
    }

    /**
     * 更新商店信息
     */
    @PatchMapping(path = "/{storeId}", consumes = "multipart/form-data")
    public ApiResponse<Void> updateStore(
            @PathVariable int storeId,
            @Valid @ModelAttribute StoreUpdateRequest params
            ) {
        storeService.updateStore(
                storeId,
                params.getName(),
                params.getDescription(),
                params.getLogo(),
                params.getAddress(),
                params.getQualifications(),
                params.getMerchantAccounts()
        );
        return ApiResponse.success();
    }
    /**
     * 删除商店
     */
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(@PathVariable int storeId) {
        storeService.deleteStore(storeId);
        return ApiResponse.success();
    }

    /**
     * 获取商店的token列表
     */
    @GetMapping("/{storeId}/tokens")
    public ApiResponse<List<TokenInfoResponse>> getTokenList(@PathVariable int storeId) {
        List<TokenInfoResponse> tokens = employmentService.getTokenList(storeId);
        return ApiResponse.success(tokens);
    }

    /**
     * 生成商店员工授权token
     */
    @PostMapping("/{storeId}/tokens")
    public ApiResponse<String> generateToken(@PathVariable int storeId,
                                             @Valid @RequestBody TokenGenerateRequest params) {
        String token = employmentService.generateToken(
                storeId,
                params.getName(),
                params.getExpireTime());
        return ApiResponse.success(token);
    }

    /**
     * 删除商店员工授权token
     */
    @DeleteMapping("/{storeId}/tokens/{tokenId}")
    public ApiResponse<Void> deleteToken(@PathVariable int storeId,
                                         @PathVariable int tokenId) {
        employmentService.deleteToken(storeId, tokenId);
        return ApiResponse.success();
    }

    /**
     * 验证商店员工授权token
     */
    @PostMapping("/{storeId}/auth")
    public ApiResponse<Void> authToken(@PathVariable int storeId,
                                       @RequestParam String token) {
        employmentService.authToken(storeId, token);
        return ApiResponse.success();
    }

    /**
     * 解雇商店员工
     */
    @DeleteMapping("/{storeId}/staff/{userId}")
    public ApiResponse<Void> dismissEmployee(@PathVariable int storeId,
                                             @PathVariable int userId,
                                             @RequestParam String reason) {
        employmentService.dismiss(storeId, userId, reason);
        return ApiResponse.success();
    }

    /**
     * 员工主动辞职
     */
    @DeleteMapping("/{storeId}/resign")
    public ApiResponse<Void> resign(@PathVariable int storeId,
                                    @RequestParam String reason) {
        employmentService.resign(storeId, reason);
        return ApiResponse.success();
    }

    /**
     * 获取商店员工列表
     */
    @GetMapping("/{storeId}/staff")
    public ApiResponse<List<UserBriefResponse>> getStaffList(@PathVariable int storeId) {
        List<UserBriefResponse> staffList = employmentService.getStaffList(storeId);
        return ApiResponse.success(staffList);
    }

    /**
     * 审核店铺（管理员权限）
     *
     * @param storeId 店铺ID
     * @param pass   是否通过审核
     * @param comment 审核意见
     * @return 操作成功与否
     */
    @PostMapping("/review")
    public ApiResponse<Void> reviewStore(
            @RequestParam int storeId,
            @RequestParam boolean pass,
            @RequestBody String comment

    ) {
        storeService.review(storeId, pass, comment);
        return ApiResponse.success();
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
    public ApiResponse<Page<StoreInfoResponse>> getAwaitingReviewStoreList(
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
    public ApiResponse<Page<StoreInfoResponse>> getSuspendedStoreList(
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

    /**
     * 获取店铺的收款账户信息
     *
     * @param storeId 店铺ID
     * @return 商户收款账户信息
     */
    @GetMapping("/{storeId}/merchant-accounts")
    public ApiResponse<Map<PaymentMethod, String>> getStoreMerchantAccounts(@PathVariable int storeId) {
        Map<PaymentMethod, String> accounts = storeService.getMerchantAccounts(storeId);
        return ApiResponse.success(accounts);
    }
}