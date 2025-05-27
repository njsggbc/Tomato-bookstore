package cn.edu.nju.TomatoMall.events.advertisement;

import cn.edu.nju.TomatoMall.models.po.Advertisement;
import lombok.Getter;

@Getter
public abstract class AdvertisementEvent {
    private final Advertisement advertisement;

    public AdvertisementEvent(Advertisement advertisement) {
        this.advertisement = advertisement;
    }
}
