package cn.edu.nju.TomatoMall.models.dto.shipment;

import cn.edu.nju.TomatoMall.enums.ShippingCompany;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ShipRequest {
    @NotBlank
    private String trackingNo;
    @NotNull
    private ShippingCompany shippingCompany;

    private String senderName;
    private String senderPhone;
    private String senderAddress;
}
