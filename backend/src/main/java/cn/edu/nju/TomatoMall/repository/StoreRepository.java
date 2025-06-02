package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.StoreStatus;
import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT s.address FROM Store s WHERE s.id = ?1")
    String findAddressById(int id);

    // 相关度排序查询
    @Query("SELECT s FROM Store s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY " +
            "CASE " +
            // 商店名称完全匹配
            "WHEN LOWER(s.name) = LOWER(:keyword) THEN 1 " +
            // 商店名称开头匹配
            "WHEN LOWER(s.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2 " +
            // 商店名称包含关键词
            "WHEN LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3 " +
            // 描述匹配
            "WHEN LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 4 " +
            // 地址匹配
            "ELSE 5 " +
            "END ASC, s.score DESC, s.createTime DESC")
    Page<Store> searchStoresByRelevance(@Param("keyword") String keyword, Pageable pageable);

    // 自定义排序查询
    @Query("SELECT s FROM Store s " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Store> searchStoresWithCustomSort(@Param("keyword") String keyword, Pageable pageable);
}