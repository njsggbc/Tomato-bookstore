package cn.edu.nju.TomatoMall.listener;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.enums.MessagePriority;
import cn.edu.nju.TomatoMall.enums.MessageType;
import cn.edu.nju.TomatoMall.events.product.*;
import cn.edu.nju.TomatoMall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductListener {
    private final MessageService messageService;

    @Autowired
    public ProductListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @EventListener
    @Transactional
    public void handleProductLowStockEvent(ProductLowStockEvent event) {
        messageService.broadcastNotificationToStore(
                MessageType.BUSINESS,
                event.getProduct().getStore(),
                "商品库存不足",
                "商品 " + event.getProduct().getName() + " 库存不足，请及时补货。",
                EntityType.PRODUCT,
                event.getProduct().getId(),
                MessagePriority.HIGH
        );
    }
}
