package cn.edu.nju.TomatoMall.models.dto.employment;

import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.po.EmploymentToken;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenInfoResponse {
    int id;
    String name;
    String createTime;
    String expireTime;
    boolean expired;
    UserBriefResponse consumer;

    public TokenInfoResponse(EmploymentToken employmentToken) {
        this.id = employmentToken.getId();
        this.name = employmentToken.getName();
        this.createTime = employmentToken.getCreatedAt().toString();
        this.expireTime = employmentToken.getExpiresAt().toString();
        this.expired = employmentToken.isValid();
        this.consumer = new UserBriefResponse(employmentToken.getConsumer());
    }
}
