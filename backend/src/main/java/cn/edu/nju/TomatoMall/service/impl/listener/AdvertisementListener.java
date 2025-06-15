package cn.edu.nju.TomatoMall.service.impl.listener;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.service.PaymentService;
import cn.edu.nju.TomatoMall.service.impl.events.advertisement.AdPlacementCancelEvent;
import cn.edu.nju.TomatoMall.service.impl.events.advertisement.AdvertisingEvent;
import cn.edu.nju.TomatoMall.service.impl.events.advertisement.AdvertisingReviewEvent;
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
    private final PaymentService paymentService;

    @Autowired
    public AdvertisementListener(MessageService messageService, UserRepository userRepository, PaymentService paymentService) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @EventListener
    public void handleAdvertisingEvent(AdvertisingEvent event) {
        List<User> administrators = userRepository.findAllByRole(Role.ADMIN);
        administrators.forEach(administrator -> {
            messageService.sendNotification(
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
        messageService.sendNotification(
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

    @EventListener
    public void handleAdvertisementCancelEvent(AdPlacementCancelEvent event) {
        Payment payment = event.getPlacement().getPayment();
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            paymentService.refund(payment.getPaymentNo(), null, "广告投放退款");
        }
    }
}
