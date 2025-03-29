package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    String username;
    String phone;
    String email;

    @NonNull
    String password;
}
