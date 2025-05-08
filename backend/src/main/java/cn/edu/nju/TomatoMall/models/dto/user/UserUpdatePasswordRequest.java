package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserUpdatePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
}
