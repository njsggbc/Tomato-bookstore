package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.AdPlacementStatus;
import cn.edu.nju.TomatoMall.models.po.AdvertisementPlacement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AdvertisementPlacementRepository extends CrudRepository<AdvertisementPlacement, Integer> {
    List<AdvertisementPlacement> findBySpaceId(int id);

    List<AdvertisementPlacement> findByStatus(AdPlacementStatus adPlacementStatus);

    List<AdvertisementPlacement> findByAdvertisementStoreId(int storeId);
}
