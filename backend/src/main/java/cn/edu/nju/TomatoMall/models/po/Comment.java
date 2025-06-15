package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.EntityType;
import lombok.*;
import org.hibernate.annotations.Check;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private EntityType entityType; // 评论的实体类型，如商品、商店

    @Column(nullable = false)
    private int entityId;  // 评论的实体ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    private Comment parent;  // 父评论，用于回复功能

    @Check(constraints = "rating IS NULL OR rating between 1 and 10")
    private Integer rating; // 评分1-10

    @ElementCollection
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Builder.Default
    private Set<Integer> likedUserIds = new HashSet<>(); // 点赞用户ID集合

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updateTime = createTime;

    public int getLikes() {
        return likedUserIds.size();
    }

    public boolean isLikedBy(int userId) {
        return likedUserIds.contains(userId);
    }

    public void toggleLike(int userId) {
        if (likedUserIds.contains(userId)) {
            likedUserIds.remove(userId);
        } else {
            likedUserIds.add(userId);
        }
    }
}