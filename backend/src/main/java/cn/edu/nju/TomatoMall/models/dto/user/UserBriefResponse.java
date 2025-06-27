package cn.edu.nju.TomatoMall.models.dto.user;

import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Data;

@Data
public class UserBriefResponse {
    private int id;
    private String username;
    private String role;
    private String avatar;

    public UserBriefResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().toString();
        this.avatar = user.getAvatarUrl();
    }
}
