package cn.edu.nju.TomatoMall.models.dto.employment;

import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.po.EmploymentToken;
import lombok.Data;

@Data
public class TokenInfoResponse {
    private int id;
    private String name;
    private String createTime;
    private String expireTime;
    private boolean expired;
    private UserBriefResponse consumer;

    public TokenInfoResponse(EmploymentToken employmentToken) {
        this.id = employmentToken.getId();
        this.name = employmentToken.getName();
        this.createTime = employmentToken.getCreateTime().toString();
        this.expireTime = employmentToken.getExpireTime() == null ? null : employmentToken.getExpireTime().toString();
        this.expired = employmentToken.isValid();
        this.consumer = employmentToken.getConsumer() == null ? null : new UserBriefResponse(employmentToken.getConsumer());
    }
}
