package cn.edu.nju.TomatoMall.models.dto.employment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class TokenGenerateRequest {
    @NotBlank
    private String name;

    private LocalDateTime expireTime;
}
