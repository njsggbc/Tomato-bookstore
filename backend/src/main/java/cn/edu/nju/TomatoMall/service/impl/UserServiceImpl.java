package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.user.UserDetailResponse;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import cn.edu.nju.TomatoMall.service.UserService;
import cn.edu.nju.TomatoMall.util.FileUtil;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final FileUtil fileUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, SecurityUtil securityUtil, FileUtil fileUtil) {
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.fileUtil = fileUtil;
    }

    @Override
    @Transactional
    public void register(String username, String phone, String password, String location, String name, String email, MultipartFile avatar) {
        validatePhone(phone);
        if (userRepository.existsByPhone(phone)) {
            throw TomatoMallException.phoneAlreadyExists();
        }

        validateUsername(username);
        if (userRepository.existsByUsername(username)) {
            throw TomatoMallException.usernameAlreadyExists();
        }

        if (email != null) {
            validateEmail(email);
            if (userRepository.existsByEmail(email)) {
                throw TomatoMallException.emailAlreadyExists();
            }
        }

        if (name != null) {
            validateName(name);
        }

       User user = User.builder()
               .phone(phone)
               .username(username)
               .email(email)
               // TODO: 加密存储
               .password(password)
               .name(name)
               .address(location)
               .role(Role.USER)
               .build();

        userRepository.save(user);

        if(avatar != null && !avatar.isEmpty()){
            user.setAvatarUrl(fileUtil.upload(user.getId(), avatar));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String phone, String email, String password) {
        User user = null;
        if (username != null ) {
            user = userRepository.findByUsername(username).orElse(null);
        } else if (phone != null) {
            user = userRepository.findByPhone(phone).orElse(null);
        } else if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            throw TomatoMallException.userNotFound();
        }

        if (!user.getPassword().equals(password)) {
            throw TomatoMallException.phoneOrPasswordError();
        }

        return securityUtil.getToken(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getDetail() {
        return new UserDetailResponse(securityUtil.getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public UserBriefResponse getBrief(int id) {
        User user = userRepository.findById(id).orElseThrow(TomatoMallException::userNotFound);

        return new UserBriefResponse(user);
    }

    @Override
    @Transactional
    public void updateInformation(String username, String name, String phone, String email, String location, MultipartFile avatar) {
        User user = securityUtil.getCurrentUser();

        if (phone != null) {
            validatePhone(phone);
            if (userRepository.existsByPhone(phone) && !phone.equals(user.getPhone())) {
                throw TomatoMallException.phoneAlreadyExists();
            }
            user.setPhone(phone);
        }

        if (username != null) {
            validateUsername(username);
            if (userRepository.existsByUsername(username) && !username.equals(user.getUsername())) {
                throw TomatoMallException.usernameAlreadyExists();
            }
            user.setUsername(username);
        }

        if (email != null) {
            validateEmail(email);
            if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
                throw TomatoMallException.emailAlreadyExists();
            }
            user.setEmail(email);
        }

        if(name != null){
           validateName(name);
            user.setName(name);
        }
        if(location != null){
            user.setAddress(location);
        }
        if(avatar != null){
            String oldAvatar = user.getAvatarUrl();
            if (oldAvatar != null) {
                fileUtil.delete(oldAvatar);
            }
            user.setAvatarUrl(fileUtil.upload(user.getId(), avatar));
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(String currentPassword, String newPassword) {
        User user = securityUtil.getCurrentUser();
        if (!user.getPassword().equals(currentPassword)) {
            throw TomatoMallException.phoneOrPasswordError();
        }
        validatePassword(newPassword);
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.isEmpty() || !phone.matches("^[1][3-9][0-9]{9}$")) {
            throw TomatoMallException.invalidParameter("手机号不合法");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            throw TomatoMallException.invalidParameter("用户名不合法");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > 50) {
            throw TomatoMallException.invalidParameter("姓名字符需在1到50个字符之间");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty() || !email.matches("^[\\w-\\.]+@[\\w-]+\\.[a-z]{2,4}$")) {
            throw TomatoMallException.invalidParameter("邮箱格式不正确");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 20) {
            throw TomatoMallException.invalidParameter("密码长度必须在6到20个字符之间");
        }
    }
}
