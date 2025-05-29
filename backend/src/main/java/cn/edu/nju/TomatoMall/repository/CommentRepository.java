package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
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
    List<Comment> findByItemIdOrderByLikesCountDesc(int itemId);
    List<Comment> findByShopIdOrderByLikesCountDesc(int shopId);
    List<Comment> findByUserId(int userId);

    Page<Comment> findByItemIdAndIsDeletedFalseOrderByCreatedAtDesc(int itemId, Pageable pageable);
    Page<Comment> findByShopIdAndIsDeletedFalseOrderByCreatedAtDesc(int shopId, Pageable pageable);
    Page<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(int userId, Pageable pageable);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.itemId = ?1 AND c.isDeleted = false")
    Double getAverageRatingForItem(int itemId);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.shopId = ?1 AND c.isDeleted = false")
    Double getAverageRatingForShop(int shopId);

    List<Comment> findByParentIdOrderByCreatedAtDesc(Integer parentId);

    List<Comment> findByCommentTypeAndIsDeletedFalseOrderByLikesCountDesc(CommentTypeEnum commentType);

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.id = ?1")
    void softDelete(int commentId);

    long countByItemIdAndIsDeletedFalse(int itemId);
    long countByShopIdAndIsDeletedFalse(int shopId);
    long countByUserIdAndIsDeletedFalse(int userId);
} 