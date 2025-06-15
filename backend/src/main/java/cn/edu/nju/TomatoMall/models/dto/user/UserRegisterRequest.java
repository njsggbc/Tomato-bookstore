package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String phone;
    @NotBlank
    private String password;

    private String location;
    private String name;
    private String email;
    private MultipartFile avatar;
}