package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByOnSaleIsTrue(Pageable pageable);
    Page<Product> findByStoreIdAndOnSaleIsTrue(int storeId, Pageable pageable);

    @Query("SELECT store.id FROM Product WHERE id = ?1")
    Optional<Integer> getStoreIdById(int productId);

    @Query("SELECT price FROM Product WHERE id = ?1")
    Optional<BigDecimal> getUnitPriceById(int id);

    Optional<Product> findByIdAndOnSaleIsTrue(int productId);

    Product getReferenceById(int id);

    boolean existsByIdAndOnSaleIsTrue(int id);

    @Modifying
    @Query("UPDATE Product p SET p.soldOut = :soldOut WHERE p.id = :id")
    void setSoldOutById(@Param("id") int id, @Param("soldOut") boolean soldOut);

    @Modifying
    @Query("UPDATE Product p SET p.sales = p.sales + :increment WHERE p.id = :id")
    void increaseSalesById(@Param("id") int id, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE Product p SET p.sales = p.sales - :decrement WHERE p.id = :id AND p.sales >= :decrement")
    void decreaseSalesById(@Param("id") int id, @Param("decrement") int decrement);
}