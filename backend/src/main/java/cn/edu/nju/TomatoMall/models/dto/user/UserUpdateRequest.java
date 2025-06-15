package cn.edu.nju.TomatoMall.models.dto.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserUpdateRequest {
    private String username;
    private String name;
    private String phone;
    private String location;
    private MultipartFile avatar;
    private String email;
}
