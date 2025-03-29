package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.enums.StoreRole;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.user.*;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import cn.edu.nju.TomatoMall.service.UserService;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    FileUtil fileUtil;


    @Override
    public Boolean register(UserRegisterRequest params) {
        User user = new User();

        String phone = params.getPhone();
        if (userRepository.findByPhone(phone).isPresent()) {
            throw TomatoMallException.phoneAlreadyExists();
        }

        String username = params.getUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw TomatoMallException.usernameAlreadyExists();
        }

        String email = params.getEmail();
        if (email != null) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw TomatoMallException.emailAlreadyExists();
            }
        }

        user.setPhone(phone);
        user.setUsername(username);
        user.setEmail(email);
        // TODO: 加密存储
        user.setPassword(params.getPassword());

        user.setName(params.getName());
        user.setAddress(params.getLocation());
        user.setCreateTime(LocalDateTime.now());
        user.setRole(Role.USER);
        userRepository.save(user);

        if(params.getAvatar() != null){
            user.setAvatarUrl(fileUtil.upload(user.getId(), params.getAvatar()));
        }

        return true;
    }

    @Override
    public String login(UserLoginRequest params) {
        User user = null;
        if (params.getUsername() != null ) {
            user = userRepository.findByUsername(params.getUsername()).get();
        } else if (params.getPhone() != null) {
            user = userRepository.findByPhone(params.getPhone()).get();
        } else if (params.getEmail() != null) {
            user = userRepository.findByEmail(params.getEmail()).get();
        }

        if (user == null) {
            throw TomatoMallException.userNotFound();
        }

        if (!user.getPassword().equals(params.getPassword())) {
            throw TomatoMallException.passwordError();
        }

        return securityUtil.getToken(user);
    }

    @Override
    public UserDetailResponse getDetail() {
        return new UserDetailResponse(securityUtil.getCurrentUser());
    }

    @Override
    public UserBriefResponse getBrief(int id) {
        User user = userRepository.findById(id).orElseThrow(TomatoMallException::userNotFound);

        return new UserBriefResponse(user);
    }

    @Override
    public Boolean updateInformation(UserUpdateRequest params) {
        User user = securityUtil.getCurrentUser();

        String phone = params.getPhone();
        String username = params.getUsername();
        String email = params.getEmail();

        if (phone != null && userRepository.findByPhone(phone).isPresent() && !phone.equals(user.getPhone())) {
            throw TomatoMallException.phoneAlreadyExists();
        }

        if (username != null && userRepository.findByUsername(username).isPresent() && !username.equals(user.getUsername())) {
            throw TomatoMallException.usernameAlreadyExists();
        }

        if (email != null && userRepository.findByEmail(email).isPresent() && !email.equals(user.getEmail())) {
            throw TomatoMallException.emailAlreadyExists();
        }

        if(phone != null){
            user.setPhone(phone);
        }
        if(username != null){
            user.setUsername(username);
        }
        if(email != null){
            user.setEmail(email);
        }
        if(params.getUsername() != null){
            user.setUsername(params.getUsername());
        }
        if(params.getName() != null){
            user.setName(params.getName());
        }
        if(params.getLocation() != null){
            user.setAddress(params.getLocation());
        }
        if(params.getAvatar() != null){
            String oldAvatar = user.getAvatarUrl();
            if (oldAvatar != null) {
                fileUtil.delete(oldAvatar);
            }
            user.setAvatarUrl(fileUtil.upload(user.getId(), params.getAvatar()));
        }

        userRepository.save(user);

        return true;
    }

    @Override
    public Boolean updatePassword(String currentPassword, String newPassword) {
        User user = securityUtil.getCurrentUser();
        if (!user.getPassword().equals(currentPassword)) {
            throw TomatoMallException.phoneOrPasswordError();
        }
        user.setPassword(newPassword);
        userRepository.save(user);

        return true;
    }

    @Override
    public String getPermission(int storeId) {
        User user = securityUtil.getCurrentUser();

        for (int i = 0; i < user.getManagedStores().size(); i++) {
            if (user.getManagedStores().get(i).getId() == storeId) {
                return StoreRole.MANAGER.toString();
            }
        }

        for (int i = 0; i < user.getWorkedStores().size(); i++) {
            if (user.getWorkedStores().get(i).getId() == storeId) {
                return StoreRole.STAFF.toString();
            }
        }

        return StoreRole.CUSTOMER.toString();
    }

    /*----------- HACK: 以下为兼容测试用方法 -----------*/

    @Override
    public String accountCreate(Map<String, String> params) {
        UserRegisterRequest registerRequest = new UserRegisterRequest();

        String username = params.get("username");
        registerRequest.setUsername(username);
        registerRequest.setPassword(params.get("password"));
        registerRequest.setName(params.get("name"));
        registerRequest.setEmail(params.get("email"));
        registerRequest.setPhone(params.get("telephone"));
        registerRequest.setLocation(params.get("location"));
        register(registerRequest);
        // 兼容测试，创建用户时可以设置角色
        User user = userRepository.findByUsername(username).get();
        user.setRole(Role.valueOf(params.get("role").toUpperCase()));
        userRepository.save(user);

        return "注册成功";
    }

    @Override
    public UserDetailResponse accountGet(String username) {
        return new UserDetailResponse(userRepository.findByUsername(username).orElseThrow(TomatoMallException::userNotFound));
    }

    @Override
    public Boolean accountUpdate(Map<String, String> params) {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUsername(params.get("username"));
        userUpdateRequest.setPhone(params.get("telephone"));
        userUpdateRequest.setEmail(params.get("email"));
        userUpdateRequest.setName(params.get("name"));
        userUpdateRequest.setLocation(params.get("location"));

        User user = userRepository.findByUsername(userUpdateRequest.getUsername()).orElseThrow(TomatoMallException::userNotFound);

        if (params.get("password") != null) {
            user.setPassword(params.get("password"));
            userRepository.save(user);
        }

        return updateInformation(userUpdateRequest);
    }

    @Override
    public String accountLogin(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(TomatoMallException::userNotFound);
        if (!user.getPassword().equals(password)) {
            throw TomatoMallException.passwordError();
        }

        return securityUtil.getToken(user);
    }
}
