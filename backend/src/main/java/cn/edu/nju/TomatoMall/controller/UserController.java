package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.user.*;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.UserService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * user
 */
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
    public ApiResponse<Void> register(@Valid @ModelAttribute UserRegisterRequest params) {
        userService.register(
                params.getUsername(),
                params.getPhone(),
                params.getPassword(),
                params.getLocation(),
                params.getName(),
                params.getEmail(),
                params.getAvatar());
        return ApiResponse.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody UserLoginRequest params, HttpServletResponse response) {
        String token = userService.login(
                params.getUsername(),
                params.getPhone(),
                params.getEmail(),
                params.getPassword());

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
    public ApiResponse<Void> updateInformation(@Valid @ModelAttribute UserUpdateRequest params) {
        userService.updateInformation(
                params.getUsername(),
                params.getName(),
                params.getPhone(),
                params.getEmail(),
                params.getLocation(),
                params.getAvatar());
        return ApiResponse.success();
    }

    /**
     * 更新用户密码
     */
    @PatchMapping("/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UserUpdatePasswordRequest payload) {
        userService.updatePassword(payload.getCurrentPassword(), payload.getNewPassword());
        return ApiResponse.success();
    }

}
