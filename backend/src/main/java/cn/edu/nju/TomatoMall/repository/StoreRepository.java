package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.models.po.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
    Store findByName(String name);
    Page<Store> findByStatus(StoreStatus status, Pageable pageable);
    Page<Store> findByStatusIn(List<StoreStatus> statuses, Pageable pageable);
}
