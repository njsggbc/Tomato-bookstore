package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterRequest {
    @NotBlank
    String username;
    @NotBlank
    String phone;
    @NotBlank
    String password;

    String location;
    String name;
    String email;
    MultipartFile avatar;
}