package cn.edu.nju.TomatoMall.service.impl.strategy;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class SimpleAdChargingStrategy implements AdChargingStrategy {
    private final BigDecimal unitPrice = new BigDecimal("10"); // 每次展示的价格
    private final SecurityUtil securityUtil;

    public SimpleAdChargingStrategy(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    @Override
    public Payment charge(AdvertisementPlacement advertisementPlacement) {
        return Payment.builder()
                .user(securityUtil.getCurrentUser())
                .entityType(EntityType.ADVERTISEMENT_PLACEMENT)
                .entityId(advertisementPlacement.getId())
                .amount(unitPrice.multiply(BigDecimal.valueOf(advertisementPlacement.getDisplayTimeList().size())))
                .build();
    }
}
