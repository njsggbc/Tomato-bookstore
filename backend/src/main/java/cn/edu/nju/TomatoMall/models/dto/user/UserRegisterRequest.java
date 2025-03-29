package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserRegisterRequest {
    @NonNull
    String username;

    @NonNull
    String phone;

    @NonNull
    String password;

    String location;
    String name;
    String email;
    MultipartFile avatar;
}