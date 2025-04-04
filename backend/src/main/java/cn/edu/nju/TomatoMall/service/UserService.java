package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.user.*;

import java.util.Map;

public interface UserService {
    /**
     * 注册
     * @param params
     * @return
     */
    Boolean register(UserRegisterRequest params);

    /**
     * 登录
     * @param params
     * @return token
     */
    String login(UserLoginRequest params);

    /**
     * 获取当前用户完整信息
     * @return
     */
    UserDetailResponse getDetail();

    /**
     * 获取指定用户简略信息
     * @param id
     * @return
     */
    UserBriefResponse getBrief(int id);

    /**
     * 更新用户信息
     * @param params
     * @return
     */
    Boolean updateInformation(UserUpdateRequest params);

    /**
     * 更新用户密码
     * @param currentPassword
     * @param newPassword
     * @return
     */
    Boolean updatePassword(String currentPassword, String newPassword);

    /**
     * 获取用户对于商店的权限
     * @param storeId
     * @return
     */
    String getPermission(int storeId);

    /*---------- HACK: 以下为兼容测试用接口 ----------*/

    String accountCreate(Map<String, String> params);

    UserDetailResponse accountGet(String username);

    Boolean accountUpdate(Map<String, String> params);

    String accountLogin(String username, String password);


}
