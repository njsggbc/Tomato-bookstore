package cn.edu.nju.TomatoMall.models.dto.employment;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenGenerateRequest {
    @NotBlank
    String name;

    String expireTime;
}
