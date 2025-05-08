package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginRequest {
    String username;
    String phone;
    String email;
    @NotBlank
    String password;
}
