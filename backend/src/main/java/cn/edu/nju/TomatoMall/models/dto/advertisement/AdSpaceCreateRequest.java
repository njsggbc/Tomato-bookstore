package cn.edu.nju.TomatoMall.models.dto.advertisement;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AdSpaceCreateRequest {
    @NotBlank
    private String label;

    @NotNull
    private AdSpaceType type;

    @Min(1)
    private Integer cycleInDay = 7; // 默认7天

    @Min(1)
    private Integer segmentInHour = 3; // 默认3小时
}