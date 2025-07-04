package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.models.po.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o FROM Order o WHERE o.id IN ?1 AND o.user.id = ?2")
    List<Order> findAllByIdsAndUserId(List<Integer> orderIds, int userId);

    Optional<Order> findByIdAndUserId(int orderId, int userId);
    Optional<Order> findByOrderNoAndUserId(String orderNo, int userId);
    Optional<Order> findByIdAndStoreId(int orderId, int storeId);
    Optional<Order> findByOrderNoAndStoreId(String orderNo, int storeId);
    Page<Order> findAllByUserIdAndStatusIn(int userId, List<OrderStatus> status, Pageable pageable);
    Page<Order> findAllByStoreIdAndStatusIn(int storeId, List<OrderStatus> status, Pageable pageable);
    Optional<Order> findByOrderNo(String orderNo);
}
