package cn.edu.nju.TomatoMall.models.po;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageStatus;
import cn.edu.nju.TomatoMall.enums.MessageType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private MessageType type;

    // 发送者(通知时为null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // 接收者(广播通知为null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private EntityType relatedEntityType; // 相关实体类型

    private Integer relatedEntityId; // 相关实体ID

    @Builder.Default
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.UNREAD;

    @Builder.Default
    private MessagePriority priority = MessagePriority.LOW;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
