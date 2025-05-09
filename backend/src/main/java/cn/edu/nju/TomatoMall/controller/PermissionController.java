package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.StoreRole;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * permission
 */
@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/store/{storeId}")
    public ApiResponse<StoreRole> getStoreRole(@PathVariable int storeId) {
        StoreRole role = permissionService.getStoreRole(storeId);
        return ApiResponse.success(role);
    }
}