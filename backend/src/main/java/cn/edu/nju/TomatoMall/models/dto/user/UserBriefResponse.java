package cn.edu.nju.TomatoMall.models.dto.user;

import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Data;

@Data
public class UserBriefResponse {
    int id;
    String username;
    String role;
    String avatar;

    public UserBriefResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().toString();
        this.avatar = user.getAvatarUrl();
    }
}
