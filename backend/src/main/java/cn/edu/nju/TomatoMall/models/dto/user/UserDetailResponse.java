package cn.edu.nju.TomatoMall.models.dto.user;

import cn.edu.nju.TomatoMall.models.dto.store.StoreBriefResponse;
import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class UserDetailResponse {

    @NonNull
    int id;

    @NonNull
    String username;

    @NonNull
    String telephone;

    @NonNull
    String role;

    @NonNull
    String regTime;

    String name;
    String location;
    String avatar;
    String email;

    List<StoreBriefResponse> manageStores;
    List<StoreBriefResponse> workStores;

    public UserDetailResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.telephone = user.getPhone();
        this.role = user.getRole().toString();
        this.location = user.getAddress();
        this.avatar = user.getAvatarUrl();
        this.email = user.getEmail();
        this.name = user.getName();
        this.manageStores = user.getManagedStores().stream().map(StoreBriefResponse::new).collect(Collectors.toList());
        this.workStores = user.getWorkedStores().stream().map(StoreBriefResponse::new).collect(Collectors.toList());
        this.regTime = user.getCreateTime().toString();
    }
}
