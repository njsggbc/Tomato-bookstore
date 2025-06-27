package cn.edu.nju.TomatoMall.service.impl;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageStatus;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import cn.edu.nju.TomatoMall.models.dto.message.MessageResponse;
import cn.edu.nju.TomatoMall.models.po.Message;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.EmploymentRepository;
import cn.edu.nju.TomatoMall.repository.MessageRepository;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import cn.edu.nju.TomatoMall.service.MessageService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import cn.edu.nju.TomatoMall.websocket.TomatoMallWebSocketHandler;
import cn.edu.nju.TomatoMall.websocket.type.TomatoMallWebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final EmploymentRepository employmentRepository;
    private final TomatoMallWebSocketHandler tomatoMallWebSocketHandler;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository,
                              UserRepository userRepository,
                              SecurityUtil securityUtil,
                              EmploymentRepository employmentRepository,
                              TomatoMallWebSocketHandler tomatoMallWebSocketHandler) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.employmentRepository = employmentRepository;
        this.tomatoMallWebSocketHandler = tomatoMallWebSocketHandler;
    }

    @Override
    @Transactional
    public void sendNotification(MessageType type, User recipient, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority) {
        if (type == null || !MessageType.isNotification(type)) {
            throw TomatoMallException.messageTypeNotSupported();
        }

        Message message = Message.builder()
                .type(type)
                .recipient(recipient)
                .title(title)
                .content(content)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .priority(priority)
                .build();

        messageRepository.save(message);

        // 使用WebSocket推送消息
        if (type != MessageType.BROADCAST) {
            tomatoMallWebSocketHandler.sendToUser(recipient.getId(), TomatoMallWebSocketMessage.newMessage(new MessageResponse(message)));
        }
    }

    @Override
    @Transactional
    public void broadcastNotificationToStore(MessageType type, Store store, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority) {
        List<User> recipients = new ArrayList<>();
        recipients.add(store.getManager());
        recipients.addAll(employmentRepository.getEmployeeByStoreId(store.getId()));

        recipients.forEach(recipient -> sendNotification(
                type,
                recipient,
                title,
                content,
                entityType,
                entityId,
                priority
        ));
    }

    @Override
    public Page<MessageResponse> getNotifications(int page, int size, MessageType type, MessageStatus status, EntityType relatedEntityType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, size, sort);

        List<MessageType> types;
        if (type == null) {
            types = MessageType.getNotificationTypes();
        } else {
            if (!MessageType.isNotification(type)) {
                throw TomatoMallException.messageTypeNotSupported();
            }
            types = Collections.singletonList(type);
        }

        return messageRepository.findByRecipientIdWithFilters(securityUtil.getCurrentUser().getId(), types, status, relatedEntityType, pageable)
                .map(MessageResponse::new);
    }

    @Override
    public void deleteNotification(int messageId) {
        Message message = messageRepository.findByIdAndRecipientId(messageId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::messageNotFound);
        if (!MessageType.isNotification(message.getType())) {
            throw TomatoMallException.messageTypeNotSupported();
        }
        messageRepository.delete(message);
    }

    @Override
    public void markAllNotificationsAsRead(MessageType type, EntityType relatedEntityType) {
        List<MessageType> types;
        if (type == null) {
            types = MessageType.getNotificationTypes();
        } else {
            if (!MessageType.isNotification(type)) {
                throw TomatoMallException.messageTypeNotSupported();
            }
            types = Collections.singletonList(type);
        }

        messageRepository.updateStatusByRecipientIdWithFilters(
                securityUtil.getCurrentUser().getId(),
                types,
                relatedEntityType,
                MessageStatus.UNREAD,
                MessageStatus.READ
        );
    }

    @Override
    public int getUnreadNotificationCount(MessageType type, EntityType relatedEntityType) {
        List<MessageType> types;
        if (type == null) {
            types = MessageType.getNotificationTypes();
        } else {
            if (!MessageType.isNotification(type)) {
                throw TomatoMallException.messageTypeNotSupported();
            }
            types = Collections.singletonList(type);
        }

        return messageRepository.countByRecipientIdWithFilters(securityUtil.getCurrentUser().getId(), types, MessageStatus.UNREAD, relatedEntityType);
    }

    @Override
    public void markMessageAsRead(int messageId) {
        Message message = messageRepository.findByIdAndRecipientId(messageId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::messageNotFound);

        if (message.getType() == MessageType.BROADCAST) {
            throw TomatoMallException.messageTypeNotSupported();
        }

        if (message.getStatus() == MessageStatus.UNREAD) {
            message.setStatus(MessageStatus.READ);
            messageRepository.save(message);
        }
    }
}
