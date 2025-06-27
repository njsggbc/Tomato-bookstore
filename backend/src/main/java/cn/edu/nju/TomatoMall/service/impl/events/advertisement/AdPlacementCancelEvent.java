package cn.edu.nju.TomatoMall.service.impl.events.advertisement;

import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;

public class AdPlacementCancelEvent extends AdvertisingEvent {
    public AdPlacementCancelEvent(AdvertisementPlacement placement) {
        super(placement);
    }
}
