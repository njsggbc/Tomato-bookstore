package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {

    // Find store by name
    Store findByName(String name);
    boolean existsByName(String name);

    // Find stores by status
    Page<Store> findByStatus(StoreStatus status, Pageable pageable);
    // HACK: 测试用
    Page<Store> findByStatusAndIdNot(StoreStatus status, Pageable pageable, int storeId);

    // Find stores with status in the provided list
    Page<Store> findByStatusIn(List<StoreStatus> statuses, Pageable pageable);

    // Get manager ID for a specific store
    @Query("SELECT s.manager.id FROM Store s WHERE s.id = ?1")
    Optional<Integer> findManagerIdById(int storeId);

    boolean existsByIdAndManagerId(int storeId, int managerId);

    List<Store> findByManagerId(int managerId);

    Store getReferenceById(@NonNull int storeId);
}