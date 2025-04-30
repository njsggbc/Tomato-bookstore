package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@NonNull
public class UserUpdatePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
