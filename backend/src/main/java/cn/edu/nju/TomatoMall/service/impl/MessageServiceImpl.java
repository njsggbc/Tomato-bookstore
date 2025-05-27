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
import cn.edu.nju.TomatoMall.websocket.handler.TomatoMallWebSocketHandler;
import cn.edu.nju.TomatoMall.websocket.message.TomatoMallWebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public void sendMessage(MessageType type, int recipientId, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority) {
        sendMessage(
                type,
                userRepository.findById(recipientId)
                        .orElseThrow(TomatoMallException::userNotFound),
                title,
                content,
                entityType,
                entityId,
                priority
        );
    }

    @Override
    @Transactional
    public void sendMessage(MessageType type, User recipient, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority) {
        Message message = Message.builder()
                .type(type)
                .title(title)
                .content(content)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .priority(priority)
                .build();

        switch(type) {
            case CHAT:
                message.setSender(securityUtil.getCurrentUser());
            case SYSTEM:
            case SHOPPING:
            case BUSINESS:
                message.setRecipient(recipient);
            case BROADCAST:
                break;
            default:
                throw TomatoMallException.messageTypeNotSupported();
        }

        messageRepository.save(message);

        // 使用WebSocket推送消息
        if (type != MessageType.BROADCAST) {
            tomatoMallWebSocketHandler.sendToUser(recipient.getId(), TomatoMallWebSocketMessage.newMessage(message));
        }
    }

    @Override
    @Transactional
    public void broadcastMessageToStore(MessageType type, Store store, String title, String content, EntityType entityType, Integer entityId, MessagePriority priority) {
        List<User> recipients = new ArrayList<>();
        recipients.add(store.getManager());
        recipients.addAll(employmentRepository.getEmployeeByStoreId(store.getId()));

        recipients.forEach(recipient -> sendMessage(
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
    public Page<MessageResponse> getUserMessages(int page, int size, MessageType type, MessageStatus status, EntityType relatedEntityType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        return messageRepository.findByRecipientIdWithFilters(securityUtil.getCurrentUser().getId(), type, status, relatedEntityType, pageable)
                .map(MessageResponse::new);
    }

    @Override
    public void markMessageAsRead(int messageId) {
        Message message = messageRepository.findByIdAndRecipientId(messageId, securityUtil.getCurrentUser().getId())
                .orElseThrow(TomatoMallException::messageNotFound);
        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);
    }

    @Override
    public void markAllMessagesAsRead(MessageType type, EntityType relatedEntityType) {
        messageRepository.updateStatusByRecipientIdWithFilters(
                securityUtil.getCurrentUser().getId(),
                type,
                relatedEntityType,
                MessageStatus.UNREAD,
                MessageStatus.READ
        );


    }

    @Override
    public int getUnreadMessageCount(MessageType type, EntityType relatedEntityType) {
        return messageRepository.countByRecipientIdWithFilters(securityUtil.getCurrentUser().getId(), type, MessageStatus.UNREAD, relatedEntityType);
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
}
