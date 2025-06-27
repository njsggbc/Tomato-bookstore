package cn.edu.nju.TomatoMall.service.impl.strategy;

import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;
import cn.edu.nju.TomatoMall.models.po.Payment;

public interface AdChargingStrategy {
    Payment charge(AdvertisementPlacement advertisementPlacement);
}
