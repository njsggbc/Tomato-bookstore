package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.ShippingCompany;
import cn.edu.nju.TomatoMall.models.po.ShippingInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ShippingInfoResponse {
    private int id;
    private String trackingNumber;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private ShippingCompany shippingCompany;
    private Map<LocalDateTime, String> logs;

    public ShippingInfoResponse(ShippingInfo shippingInfo) {
        this.id = shippingInfo.getId();
        this.trackingNumber = shippingInfo.getTrackingNumber();
        this.deliveryAddress = shippingInfo.getDeliveryAddress();
        this.recipientName = shippingInfo.getRecipientName();
        this.recipientPhone = shippingInfo.getRecipientPhone();
        this.shippingCompany = shippingInfo.getShippingCompany();
        this.logs = new LinkedHashMap<>();
        logs.putAll(shippingInfo.getLogs());
    }
}
