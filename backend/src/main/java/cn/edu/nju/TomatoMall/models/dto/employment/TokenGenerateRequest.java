package cn.edu.nju.TomatoMall.models.dto.employment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class TokenGenerateRequest {
    @NotBlank
    String name;

    LocalDateTime expireTime;
}
