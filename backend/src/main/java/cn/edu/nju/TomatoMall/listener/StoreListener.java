package cn.edu.nju.TomatoMall.listener;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.events.store.*;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import cn.edu.nju.TomatoMall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class StoreListener {
    private final MessageService messageService;
    private final UserRepository userRepository;

    @Autowired
    public StoreListener(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void handleStoreCreateEvent(StoreStatusChangeEvent event) {
        List<User> administrators = userRepository.findAllByRole(Role.ADMIN);
        administrators.forEach(administrator -> {
            messageService.sendNotification(
                    MessageType.BUSINESS,
                    administrator,
                    "店铺审核通知",
                    "有新的店铺待审核，请及时处理。",
                    EntityType.STORE,
                    event.getStore().getId(),
                    MessagePriority.HIGH
            );
        });
    }

    @EventListener
    @Transactional
    public void handleStoreReviewEvent(StoreReviewEvent event) {
        messageService.sendNotification(
                MessageType.BUSINESS,
                event.getStore().getManager(),
                "店铺审核结果",
                (event.isPass() ? "您的店铺已通过审核\n" : "您的店铺未通过审核\n") + "审核意见：" + event.getComment(),
                EntityType.STORE,
                event.getStore().getId(),
                MessagePriority.MEDIUM
        );
    }
}
