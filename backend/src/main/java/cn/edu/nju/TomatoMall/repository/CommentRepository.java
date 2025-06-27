package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.models.po.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    /**
     * 根据实体类型和实体ID查找评论（不包括回复）
     */
    Page<Comment> findByEntityTypeAndEntityIdAndParentIsNull(
            EntityType entityType, int entityId, Pageable pageable);

    /**
     * 根据父评论ID查找回复
     */
    Page<Comment> findByParentId(int parentId, Pageable pageable);

    List<Comment> findListByParentId(int parentId);

    /**
     * 根据实体类型和实体ID统计评论数量（不包括回复）
     */
    long countByEntityTypeAndEntityIdAndParentIsNull(EntityType entityType, int entityId);

    /**
     * 计算实体的平均评分
     */
    @Query("SELECT AVG(CAST(c.rating AS double)) FROM Comment c " +
            "WHERE c.entityType = :entityType AND c.entityId = :entityId " +
            "AND c.rating IS NOT NULL AND c.parent IS NULL")
    Double calculateAverageRating(@Param("entityType") EntityType entityType,
                                  @Param("entityId") int entityId);

    /**
     * 查找用户对特定实体的评论
     */
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId " +
            "AND c.entityType = :entityType AND c.entityId = :entityId " +
            "AND c.parent IS NULL")
    Optional<Comment> findUserCommentOnEntity(@Param("userId") int userId,
                                              @Param("entityType") EntityType entityType,
                                              @Param("entityId") int entityId);

    void deleteAllByParentId(int parentId);
}