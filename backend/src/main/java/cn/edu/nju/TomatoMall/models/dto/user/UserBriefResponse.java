package cn.edu.nju.TomatoMall.models.dto.user;

import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserBriefResponse {

    @NonNull
    int id;

    @NonNull
    String username;

    @NonNull
    String role;

    String avatar;

    public UserBriefResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().toString();
        this.avatar = user.getAvatarUrl();
    }
}
