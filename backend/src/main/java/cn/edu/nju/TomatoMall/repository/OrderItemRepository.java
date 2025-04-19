package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @Query("SELECT o FROM OrderItem o WHERE o.user.id = ?1 AND o.order IS NULL")
    List<OrderItem> findCartItemsByUserId(int userId);

    @Query("SELECT o FROM OrderItem o WHERE o.id IN ?1 AND o.order IS NULL AND o.user.id = ?2")
    List<OrderItem> findCartItemsByIdsAndUserId(List<Integer> ids, int userId);

    @Query("SELECT o FROM OrderItem o WHERE o.user.id = ?1 AND o.product.id = ?2 AND o.order IS NULL")
    Optional<OrderItem> findCartItemByUserIdAndProductId(int userId, int productId);

    @Modifying
    @Query("UPDATE OrderItem o SET o.quantity = ?2 WHERE o.id = ?1 AND o.user.id = ?2 AND o.order IS NULL")
    int updateCartItemQuantityByIdAndUserId(int id, int userId, int quantity);

    @Modifying
    @Query("DELETE FROM OrderItem o WHERE o.id = ?1 AND o.user.id = ?2 AND o.order IS NULL")
    void deleteCartItemByIdAndUserId(int id, int user_id);
}
