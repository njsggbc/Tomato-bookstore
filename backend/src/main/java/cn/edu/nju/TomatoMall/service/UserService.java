package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.user.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {
    void register(String username, String phone, String password, String location, String name, String email, MultipartFile avatar);

    String login(String username, String phone, String email, String password);

    UserDetailResponse getDetail();

    UserBriefResponse getBrief(int id);

    void updateInformation(String username, String name, String phone, String email, String location, MultipartFile avatar);

    void updatePassword(String currentPassword, String newPassword);

    /*---------- HACK: 以下为兼容测试用接口 ----------*/

    String accountCreate(Map<String, String> params);

    UserDetailResponse accountGet(String username);

    void accountUpdate(Map<String, String> params);

    String accountLogin(String username, String password);


}
