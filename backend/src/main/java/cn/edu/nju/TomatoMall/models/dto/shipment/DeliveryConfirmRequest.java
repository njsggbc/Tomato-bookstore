package cn.edu.nju.TomatoMall.models.dto.shipment;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class DeliveryConfirmRequest {
    @NotNull
    private LocalDateTime deliveryTime;    // 送达时间
    @NotBlank
    private String deliveryLocation;       // 送达地点
    @NotBlank
    private String signedBy;               // 签收人
    @NotBlank
    private String phone;                  // 联系电话
    private String remark;                 // 备注
}
