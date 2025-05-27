package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessageStatus;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.models.dto.message.MessageResponse;
import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import cn.edu.nju.TomatoMall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * message
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    /**
     * 获取用户通知列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param type 消息类型
     * @param status 消息状态
     * @param relatedEntityType 相关实体类型
     * @return 用户通知列表
     */
    @GetMapping("/notifications")
    public ApiResponse<Page<MessageResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) MessageType type,
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) EntityType relatedEntityType
    ) {
        return ApiResponse.success(messageService.getNotifications(
                page,
                size,
                type,
                status,
                relatedEntityType
        ));
    }

    /**
     * 删除通知
     *
     * @param messageId 消息ID
     * @return 成功响应
     */
    @DeleteMapping("/notifications/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable int messageId
    ) {
        messageService.deleteNotification(messageId);
        return ApiResponse.success();
    }

    /**
     * 获取未读通知数量
     *
     * @param type 消息类型
     * @param relatedEntityType 相关实体类型
     * @return 未读通知数量
     */
    @GetMapping("/notifications/unread/count")
    public ApiResponse<Integer> getUnreadNotificationCount(
            @RequestParam(required = false) MessageType type,
            @RequestParam(required = false) EntityType relatedEntityType
    ) {
        return ApiResponse.success(messageService.getUnreadNotificationCount(type, relatedEntityType));
    }

    /**
     * 标记所有指定类型的通知为已读
     *
     * @param type 消息类型
     * @param relatedEntityType 相关实体类型
     */
    @PostMapping("/notifications/read/all")
    public ApiResponse<Void> markAllNotificationsAsRead(
            @RequestParam(required = false) MessageType type,
            @RequestParam(required = false) EntityType relatedEntityType
    ) {
        messageService.markAllNotificationsAsRead(type, relatedEntityType);
        return ApiResponse.success();
    }

    /**
     * 标记消息为已读
     * @param messageId
     */
    @PostMapping("/read/{messageId}")
    public ApiResponse<Void> markMessagesAsRead(
            @PathVariable int messageId
    ) {
        messageService.markMessageAsRead(messageId);
        return ApiResponse.success();
    }
}
