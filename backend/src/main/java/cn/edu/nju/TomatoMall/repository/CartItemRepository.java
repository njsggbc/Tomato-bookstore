package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    @Query("SELECT c FROM CartItem c WHERE c.id IN ?1 AND c.user.id = ?2")
    List<CartItem> findByIdsAndUserId(List<Integer> cartItemIds, int userId);

    Page<CartItem> findAllByUserId(int id, Pageable pageable);

    void deleteByIdAndUserId(int cartItemId, int userId);

    @Modifying
    @Query("UPDATE CartItem c SET c.quantity = ?3 WHERE c.id = ?1 AND c.user.id = ?2")
    void updateCartItemQuantityByIdAndUserId(int id, int userId, int quantity);
}

