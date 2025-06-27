package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.ShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, Integer> {
    Optional<ShippingInfo> findByTrackingNumber(String trackingNumber);
}
