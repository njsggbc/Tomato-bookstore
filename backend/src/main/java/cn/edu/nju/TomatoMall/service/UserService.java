package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.user.UserDetailResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理服务接口
 */
public interface UserService {
    /**
     * 注册用户
     * @param username 用户名
     * @param phone 电话
     * @param password 密码
     * @param location 位置
     * @param name 姓名
     * @param email 电子邮件
     * @param avatar 头像
     */
    void register(String username, String phone, String password, String location, String name, String email, MultipartFile avatar);

    /**
     * 用户登录
     * @param username 用户名
     * @param phone 电话
     * @param email 电子邮件
     * @param password 密码
     * @return 登录令牌
     */
    String login(String username, String phone, String email, String password);

    /**
     * 获取当前用户完整信息
     * @return 用户详细信息
     */
    UserDetailResponse getDetail();

    /**
     * 获取指定用户简略信息
     * @param id 用户ID
     * @return 用户简要信息
     */
    UserBriefResponse getBrief(int id);

    /**
     * 更新用户信息
     * @param username 用户名
     * @param name 姓名
     * @param phone 电话
     * @param email 电子邮件
     * @param location 位置
     * @param avatar 头像
     */
    void updateInformation(String username, String name, String phone, String email, String location, MultipartFile avatar);

    /**
     * 更新用户密码
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     */
    void updatePassword(String currentPassword, String newPassword);
}