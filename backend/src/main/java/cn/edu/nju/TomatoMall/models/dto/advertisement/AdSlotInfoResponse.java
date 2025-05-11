package cn.edu.nju.TomatoMall.models.dto.advertisement;

import cn.edu.nju.TomatoMall.models.po.AdvertisementSlot;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdSlotInfoResponse {
    private int id;
    private LocalDateTime startTime;
    private Boolean available;
    private Boolean active;
    private Integer adId; // NULL表示未投放广告

    // 构造方法
    public AdSlotInfoResponse(AdvertisementSlot adSlot) {
        this.id = adSlot.getId();
        this.startTime = adSlot.getStartTime();
        this.available = adSlot.isAvailable();
        this.active = adSlot.isActive();
        this.adId = adSlot.getAdvertisement() == null ? null : adSlot.getAdvertisement().getId();
    }
}