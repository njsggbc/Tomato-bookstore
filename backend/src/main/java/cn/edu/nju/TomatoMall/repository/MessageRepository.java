package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessageStatus;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.models.po.Message;
import io.micrometer.core.instrument.binder.db.MetricsDSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    Optional<Message> findByIdAndRecipientId(int id, int recipientId);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.recipient IS NOT NULL AND m.recipient.id = :recipientId) " +
            "AND (:type IS NULL OR m.type = :type) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:entityType IS NULL OR m.relatedEntityType = :entityType)")
    Page<Message> findByRecipientIdWithFilters(@Param("recipientId") int recipientId,
                                               @Param("type") MessageType type,
                                               @Param("status") MessageStatus status,
                                               @Param("entityType") EntityType entityType,
                                               Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "(m.recipient IS NOT NULL AND m.recipient.id = :recipientId) " +
            "AND (:type IS NULL OR m.type = :type) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:entityType IS NULL OR m.relatedEntityType = :entityType)")
    int countByRecipientIdWithFilters(@Param("recipientId") int recipientId,
                                      @Param("type") MessageType type,
                                      @Param("status") MessageStatus status,
                                      @Param("entityType") EntityType entityType);

    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus " +
            "WHERE (m.recipient IS NOT NULL AND m.recipient.id = :recipientId) " +
            "AND (:type IS NULL OR m.type = :type) " +
            "AND (:entityType IS NULL OR m.relatedEntityType = :entityType) " +
            "AND m.status = :status")
    void updateStatusByRecipientIdWithFilters(@Param("recipientId") int recipientId,
                                      @Param("type") MessageType type,
                                      @Param("entityType") EntityType entityType,
                                      @Param("status") MessageStatus status,
                                      @Param("newStatus") MessageStatus newStatus);
}
