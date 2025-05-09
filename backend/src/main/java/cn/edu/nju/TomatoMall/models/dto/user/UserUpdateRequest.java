package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserUpdateRequest {
    String username;
    String name;
    String phone;
    String location;
    MultipartFile avatar;
    String email;
}
