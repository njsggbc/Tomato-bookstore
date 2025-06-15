package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query("SELECT c FROM Comment c WHERE c.product.id = ?1 AND c.isDeleted = false ORDER BY c.likesCount DESC")
    Page<Comment> findByProductIdOrderByLikesCountDesc(int productId,Pageable pageable);
    @Query("SELECT c FROM Comment c WHERE c.store.id = ?1 AND c.isDeleted = false ORDER BY c.likesCount DESC")
    Page<Comment> findByStoreIdOrderByLikesCountDesc(int storeId,Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.product.id = ?1 AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByProductIdAndIsDeletedFalseOrderByCreatedAtDesc(int productId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.store.id = ?1 AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(int storeId, Pageable pageable);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.product.id = ?1 AND c.isDeleted = false")
    Double getAverageRatingForItem(int productId);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.store.id = ?1 AND c.isDeleted = false")
    Double getAverageRatingForShop(int storeId);
    @Query("SELECT c FROM Comment c WHERE c.id = ?1  ORDER BY c.likesCount DESC")
    List<Comment> findByCommentIdOrderByLikesCountDesc(Integer parentUserId);

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.id = ?1")
    void softDelete(int commentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.product.id = ?1 AND c.isDeleted = false")
    long countByProductIdAndIsDeletedFalse(int productId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.store.id = ?1 AND c.isDeleted = false")
    long countByStoreIdAndIsDeletedFalse(int storeId);
} 