package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.user.*;
import cn.edu.nju.TomatoMall.service.UserService;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 用户注册
     */
    @PostMapping(path = "/register", consumes = "multipart/form-data")
    public ApiResponse<Boolean> register(@ModelAttribute UserRegisterRequest params) {
        return ApiResponse.success(userService.register(params));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody UserLoginRequest params, HttpServletResponse response) {
        String token = userService.login(params);
        response.addCookie(securityUtil.getCookie(token));
        return ApiResponse.success(token);
    }

    /**
     * 获取当前用户完整信息
     */
    @GetMapping
    public ApiResponse<UserDetailResponse> getDetail() {
        return ApiResponse.success(userService.getDetail());
    }

    /**
     * 获取指定用户简略信息
     */
    @GetMapping("/{id}")
    public ApiResponse<UserBriefResponse> getBrief(@PathVariable("id") int id) {
        return ApiResponse.success(userService.getBrief(id));
    }

    /**
     * 更新用户信息
     */
    @PutMapping(consumes = "multipart/form-data")
    public ApiResponse<Boolean> updateInformation(@ModelAttribute UserUpdateRequest params) {
        return ApiResponse.success(userService.updateInformation(params));
    }

    /**
     * 更新用户密码
     */
    @PatchMapping("/password")
    public ApiResponse<Boolean> updatePassword(@RequestParam("currentPassword") String currentPassword,
                                               @RequestParam("newPassword") String newPassword) {
        return ApiResponse.success(userService.updatePassword(currentPassword, newPassword));
    }

    /**
     * 获取当前用户在某个商店的权限
     */
    @GetMapping("/permission")
    public ApiResponse<String> getPermission(@RequestParam("storeId") int storeId) {
        return ApiResponse.success(userService.getPermission(storeId));
    }

}
