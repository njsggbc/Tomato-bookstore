package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.InventoryStatus;
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
    @Query("UPDATE Product p SET p.inventoryStatus = :inventoryStatus WHERE p.id = :id")
    void setInventoryStatusById(@Param("id") int id, @Param("inventoryStatus") InventoryStatus inventoryStatus);

    @Modifying
    @Query("UPDATE Product p SET p.sales = p.sales + :increment WHERE p.id = :id")
    void increaseSalesById(@Param("id") int id, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE Product p SET p.sales = p.sales - :decrement WHERE p.id = :id AND p.sales >= :decrement")
    void decreaseSalesById(@Param("id") int id, @Param("decrement") int decrement);

    @Query("SELECT p.store.id FROM Product p WHERE p.id = :id")
    Optional<Integer> findStoreIdById(int id);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.specifications spec " +
            "WHERE p.onSale = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(spec) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "ORDER BY " +
            "CASE " +
            // 商品名称完全匹配 - 最高优先级
            "WHEN LOWER(p.name) = LOWER(:keyword) THEN 1 " +
            // 商品名称开头匹配
            "WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2 " +
            // 商品名称包含关键词
            "WHEN LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3 " +
            // 描述开头匹配 - 中等优先级
            "WHEN LOWER(p.description) LIKE LOWER(CONCAT(:keyword, '%')) THEN 4 " +
            // 描述包含关键词 - 中等优先级
            "WHEN LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 5 " +
            // 规格信息匹配
            "ELSE 6 " +
            "END ASC, p.sales DESC, p.rate DESC, p.createTime DESC")
    Page<Product> searchProductsByRelevance(@Param("keyword") String keyword,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.specifications spec " +
            "WHERE p.onSale = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(spec) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProductsWithCustomSort(@Param("keyword") String keyword,
                                               @Param("minPrice") BigDecimal minPrice,
                                               @Param("maxPrice") BigDecimal maxPrice,
                                               Pageable pageable);
}