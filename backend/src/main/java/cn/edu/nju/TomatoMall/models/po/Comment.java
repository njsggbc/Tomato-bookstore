package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.CommentTypeEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "item_id")
    private int itemId;

    @Column(name = "shop_id")
    private int shopId;
    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private CommentTypeEnum commentType;

    @Column(name = "parent_id")
    private Integer parentId;  // 父评论ID，用于回复功能

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;  // 软删除标记

    @Column(name = "rating", nullable = false)
    private int rating = 5;  // 评分 1-5

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 