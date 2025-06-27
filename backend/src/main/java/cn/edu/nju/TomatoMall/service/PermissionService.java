package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.StoreRole;

/**
 * 权限管理服务接口
 */
public interface PermissionService {
    /**
     * 获取用户在商店中的角色
     * @param storeId 商店ID
     * @return 商店角色
     */
    StoreRole getStoreRole(int storeId);
}
