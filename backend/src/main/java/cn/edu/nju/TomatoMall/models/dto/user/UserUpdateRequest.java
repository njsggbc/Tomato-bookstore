package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserUpdateRequest {
    String username;
    String name;
    String phone;
    String location;
    MultipartFile avatar;
    String email;
}
