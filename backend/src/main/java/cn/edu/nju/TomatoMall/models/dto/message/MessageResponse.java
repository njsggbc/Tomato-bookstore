package cn.edu.nju.TomatoMall.models.dto.message;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageStatus;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.models.po.Message;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private int id;
    private MessageType type;
    private Integer senderId;
    private Integer recipientId;
    private String title;
    private String content;
    private EntityType relatedEntityType;
    private Integer relatedEntityId;
    private MessageStatus status;
    private LocalDateTime timestamp;
    private MessagePriority priority;
    public MessageResponse(Message message) {
        this.id = message.getId();
        this.type = message.getType();
        this.senderId = message.getSender() != null ? message.getSender().getId() : null;
        this.recipientId = message.getRecipient() != null ? message.getRecipient().getId() : null;
        this.title = message.getTitle();
        this.content = message.getContent();
        this.relatedEntityType = message.getRelatedEntityType();
        this.relatedEntityId = message.getRelatedEntityId();
        this.status = message.getStatus();
        this.timestamp = message.getCreateTime();
        this.priority = message.getPriority();
    }

}
