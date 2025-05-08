package cn.edu.nju.TomatoMall.models.dto.shipment;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class ShippingUpdateRequest {
    @NotBlank
    private String logMessage;             // 物流信息
    @NotBlank
    private LocalDateTime logTime;         // 记录时间
    @NotBlank
    private String location;               // 当前位置
    @NotBlank
    private String operatorName;           // 操作人员
}