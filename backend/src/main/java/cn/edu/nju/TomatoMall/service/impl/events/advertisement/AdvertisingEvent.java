package cn.edu.nju.TomatoMall.service.impl.events.advertisement;

import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;
import lombok.Getter;

@Getter
public class AdvertisingEvent extends AdvertisementEvent {
    private final AdvertisementPlacement placement;

    public AdvertisingEvent(AdvertisementPlacement placement) {
        super(placement.getAdvertisement());
        this.placement = placement;
    }
}
