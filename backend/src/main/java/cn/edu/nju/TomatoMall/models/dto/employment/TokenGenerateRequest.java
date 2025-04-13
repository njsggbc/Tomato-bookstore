package cn.edu.nju.TomatoMall.models.dto.employment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenGenerateRequest {
    @NonNull
    String name;

    String expireTime;
}
