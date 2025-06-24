package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.dto.store.StoreInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 商店管理服务接口
 */
public interface StoreService {
    /**
     * 获取商店列表
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @return 商店信息分页
     */
    Page<StoreInfoResponse> getStoreList(int page, int size, String field, boolean order);

    /**
     * 获取当前用户管理的商店列表
     * @return 商店信息列表
     */
    List<StoreInfoResponse> getManagedStoreList();

    /**
     * 获取当前用户工作的商店列表
     * @return 商店信息列表
     */
    List<StoreInfoResponse> getWorkedStoreList();

    /**
     * 获取商店信息
     * @param storeId 商店ID
     * @return 商店信息
     */
    StoreInfoResponse getInfo(int storeId);

    /**
     * 创建商店
     * @param name 商店名称
     * @param description 商店描述
     * @param logo 商店标志
     * @param address 商店地址
     * @param qualifications 商店资质文件
     * @param merchantAccounts 商户收款账户信息
     */
    void createStore(String name, String description, MultipartFile logo, String address, List<MultipartFile> qualifications, Map<PaymentMethod, String> merchantAccounts);

    /**
     * 更新商店信息
     * @param storeId 商店ID
     * @param name 商店名称
     * @param description 商店描述
     * @param logo 商店标志
     * @param address 商店地址
     * @param qualifications 商店资质文件
     * @param merchantAccounts 商户收款账号信息
     */
    void updateStore(int storeId, String name, String description, MultipartFile logo, String address, List<MultipartFile> qualifications, Map<PaymentMethod, String> merchantAccounts);

    /**
     * 删除商店
     * @param storeId 商店ID
     */
    void deleteStore(int storeId);

    /**
     * 审核商店（管理员权限）
     * @param storeId 商店ID
     * @param pass 是否通过
     * @param comment 审核意见
     */
    void review(int storeId, boolean pass, String comment);

    /**
     * 获取待审核的店铺列表（系统管理员权限级别）
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @return 商店信息分页
     */
    Page<StoreInfoResponse> getAwaitingReviewStoreList(int page, int size, String field, boolean order);

    /**
     * 获取被暂停的店铺列表（系统管理员权限级别）
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式
     * @return 商店信息分页
     */
    Page<StoreInfoResponse> getSuspendedStoreList(int page, int size, String field, boolean order);

    /**
     * 获取店铺的资质信息（系统管理员权限级别）
     * @param storeId 商店ID
     * @return 资质URL列表
     */
    List<String> getStoreQualification(int storeId);

    /**
     * 获取店铺的商户收款账号信息（商店管理员权限级别）
     * @param storeId 商店ID
     * @return 商户收款账号信息
     */
    Map<PaymentMethod, String> getMerchantAccounts(int storeId);
}
