package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.user.UserDetailResponse;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.UserService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * HACK: 兼容测试用用户接口
 */

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping(path = "/{username}")
    public ApiResponse<UserDetailResponse> getAccount(@PathVariable String username) {
        return ApiResponse.success(userService.accountGet(username));
    }

    @PostMapping
    public ApiResponse<String> createAccount(@RequestBody Map<String, String> params) {
        return ApiResponse.success(userService.accountCreate(params));
    }

    @PostMapping(path = "/login")
    public ApiResponse<String> login(@RequestBody Map<String ,String> params, HttpServletResponse response) {
        String token = userService.accountLogin(params.get("username"), params.get("password"));
        response.addCookie(securityUtil.getCookie(token));
        return ApiResponse.success(token);
    }

    @PutMapping
    public ApiResponse<Boolean> updateAccount(@RequestBody Map<String, String> params) {
        return ApiResponse.success(userService.accountUpdate(params));
    }



}
