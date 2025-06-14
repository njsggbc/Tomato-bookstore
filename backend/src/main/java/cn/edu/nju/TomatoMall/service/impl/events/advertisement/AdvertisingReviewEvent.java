package cn.edu.nju.TomatoMall.service.impl.events.advertisement;

import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;
import lombok.Getter;

@Getter
public class AdvertisingReviewEvent extends AdvertisementEvent {
    private final AdvertisementPlacement placement;
    private final boolean passed;
    private final String comment;

    public AdvertisingReviewEvent(AdvertisementPlacement placement, boolean passed, String comment) {
        super(placement.getAdvertisement());
        this.placement = placement;
        this.passed = passed;
        this.comment = comment;
    }
}
