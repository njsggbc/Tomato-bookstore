package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.AdSpaceType;
import cn.edu.nju.TomatoMall.models.po.AdvertisementSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvertisementSpaceRepository extends JpaRepository<AdvertisementSpace, Integer> {
    List<AdvertisementSpace> findByType(AdSpaceType adSpaceType);

    Optional<Object> findByLabel(String label);
}
