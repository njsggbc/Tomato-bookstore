package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Integer> {
    Page<Advertisement> findAll(Pageable pageable);

    List<Advertisement> findByStoreId(int storeId);
}
