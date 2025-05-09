package cn.edu.nju.TomatoMall.models.dto.user;

import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Data;

@Data
public class UserDetailResponse {
    int id;
    String username;
    String telephone;
    String role;
    String regTime;
    String name;
    String location;
    String avatar;
    String email;

    public UserDetailResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.telephone = user.getPhone();
        this.role = user.getRole().toString();
        this.location = user.getAddress();
        this.avatar = user.getAvatarUrl();
        this.email = user.getEmail();
        this.name = user.getName();
        this.regTime = user.getCreateTime().toString();
    }
}
