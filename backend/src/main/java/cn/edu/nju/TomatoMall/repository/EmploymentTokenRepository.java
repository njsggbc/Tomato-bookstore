package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.EmploymentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentTokenRepository extends JpaRepository<EmploymentToken, Integer> {

    // Find all tokens for a specific store
    @Query("SELECT et FROM EmploymentToken et WHERE et.store.id = :storeId")
    List<EmploymentToken> findAllByStoreId(@Param("storeId") int storeId);

    boolean existsByTokenAndStoreId(String token, int storeId);

    // Find a token by ID and store ID
    @Query("SELECT et FROM EmploymentToken et WHERE et.id = :tokenId AND et.store.id = :storeId")
    Optional<EmploymentToken> findByIdAndStoreId(@Param("tokenId") int tokenId, @Param("storeId") int storeId);

    // Find a valid token by token value and store ID
    @Query("SELECT et FROM EmploymentToken et WHERE et.token = :token AND et.store.id = :storeId AND et.valid = TRUE")
    Optional<EmploymentToken> findValidByTokenAndStoreId(@Param("token") String token, @Param("storeId") int storeId);
}