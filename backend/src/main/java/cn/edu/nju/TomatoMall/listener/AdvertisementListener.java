package cn.edu.nju.TomatoMall.listener;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.enums.Role;
import cn.edu.nju.TomatoMall.events.advertisement.AdvertisingEvent;
import cn.edu.nju.TomatoMall.events.advertisement.AdvertisingReviewEvent;
import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.repository.UserRepository;
import cn.edu.nju.TomatoMall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdvertisementListener {
    private final MessageService messageService;
    private final UserRepository userRepository;

    @Autowired
    public AdvertisementListener(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleAdvertisingEvent(AdvertisingEvent event) {
        List<User> administrators = userRepository.findAllByRole(Role.ADMIN);
        administrators.forEach(administrator -> {
            messageService.sendMessage(
                    MessageType.BUSINESS,
                    administrator,
                    "广告发布通知",
                    "有新的广告发布，请及时审核",
                    EntityType.ADVERTISEMENT,
                    event.getAdvertisement().getId(),
                    MessagePriority.HIGH
            );
        });
    }

    @EventListener
    public void handleAdvertisingReviewEvent(AdvertisingReviewEvent event) {
        messageService.sendMessage(
                MessageType.BUSINESS,
                event.getAdvertisement().getStore().getManager(),
                "广告投放审核结果",
                "您的广告《" + event.getAdvertisement().getTitle() + "》" + (event.isPassed() ? "已通过投放审核" : "未通过投放审核") + "\n" +
                        "审核意见：" + event.getComment(),
                EntityType.ADVERTISEMENT,
                event.getAdvertisement().getId(),
                MessagePriority.MEDIUM
        );
    }
}
