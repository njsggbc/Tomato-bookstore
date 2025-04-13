package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.StoreRole;
import org.springframework.stereotype.Service;

@Service
public interface PermissionService {
    StoreRole getStoreRole(int storeId);
}
