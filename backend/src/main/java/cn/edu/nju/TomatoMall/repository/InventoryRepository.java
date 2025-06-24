package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.Inventory;
import cn.edu.nju.TomatoMall.models.po.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProduct(Product product);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") int productId);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity, i.lockedQuantity = i.lockedQuantity - :quantity WHERE i.product.id = :productId AND i.version = :version")
    int decreaseStock(@Param("productId") int productId, @Param("quantity") Integer quantity, @Param("version") long version);

    @Modifying
    @Query("UPDATE Inventory i SET i.lockedQuantity = i.lockedQuantity + :quantity WHERE i.product.id = :productId AND (i.quantity - i.lockedQuantity) >= :quantity AND i.version = :version")
    int lockStock(@Param("productId") int productId, @Param("quantity") Integer quantity, @Param("version") long version);

    @Modifying
    @Query("UPDATE Inventory i SET i.lockedQuantity = i.lockedQuantity - :quantity WHERE i.product.id = :productId AND i.lockedQuantity >= :quantity AND i.version = :version")
    int unlockStock(@Param("productId") int productId, @Param("quantity") Integer quantity, @Param("version") long version);

    @Query("SELECT i.quantity - i.lockedQuantity FROM Inventory i WHERE i.product.id = :productId")
    int getAvailableStockById(@Param("productId") int productId);

    @Modifying
    @Query("UPDATE Inventory i SET i.thresholdQuantity = :thresholdQuantity WHERE i.product.id = :productId")
    void updateThresholdQuantityByProductId(int productId, int thresholdQuantity);

    Optional<Inventory> findByProductId(int productId);
}