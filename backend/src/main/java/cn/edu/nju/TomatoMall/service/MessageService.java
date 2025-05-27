package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageStatus;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.models.dto.message.*;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import org.springframework.data.domain.Page;

public interface MessageService {
    /**
     * 发送消息
     * @param type 消息类型
     * @param recipientId 接收者ID
     * @param title 消息标题
     * @param content 消息内容
     * @param entityType 相关实体类型
     * @param entityId 相关实体ID
     * @param priority 消息优先级
     */
    void sendMessage(MessageType type, int recipientId, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority);

    /**
     * 发送消息给指定用户
     * @param type 消息类型
     * @param recipient 接收者用户
     * @param title 消息标题
     * @param content 消息内容
     * @param entityType 相关实体类型
     * @param entityId 相关实体ID
     * @param priority 消息优先级
     */
    void sendMessage(MessageType type, User recipient, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority);

    /**
     * 向指定商店的所有用户组播消息
     * @param type 消息类型
     * @param store 商店
     * @param title 消息标题
     * @param content 消息内容
     * @param entityType 相关实体类型
     * @param entityId 相关实体ID
     * @param priority 消息优先级
     */
    void broadcastMessageToStore(MessageType type, Store store, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority);

    /**
     * 获取用户的消息列表
     * @param page 页码
     * @param size 每页大小
     * @param type 消息类型
     * @param status 消息状态
     * @param relatedEntityType 相关实体类型
     * @return 消息信息分页
     */
    Page<MessageResponse> getUserMessages(int page, int size, MessageType type, MessageStatus status, EntityType relatedEntityType);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     */
    void markMessageAsRead(int messageId);

    /**
     * 标记所有指定类型的消息为已读
     * @param type 消息类型
     * @param relatedEntityType 相关实体类型
     */
    void markAllMessagesAsRead(MessageType type, EntityType relatedEntityType);

    /**
     * 获取未读消息数量
     * @param type 消息类型
     * @param relatedEntityType 相关实体类型
     * @return 未读消息数量
     */
    int getUnreadMessageCount(MessageType type, EntityType relatedEntityType);

    /**
     * 删除通知
     * @param messageId 消息ID
     */
    void deleteNotification(int messageId);
}
