package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.AdvertisementSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementSlotRepository extends JpaRepository<AdvertisementSlot, Integer> {
    boolean existsByAdvertisementIdAndActive(int advertisementId, boolean active);
}
