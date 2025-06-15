package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginRequest {
    private String username;
    private String phone;
    private String email;
    @NotBlank
    private String password;
}
