package cn.edu.nju.TomatoMall.listener;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.events.order.OrderCancelEvent;
import cn.edu.nju.TomatoMall.events.order.OrderConfirmEvent;
import cn.edu.nju.TomatoMall.events.order.OrderDeliverEvent;
import cn.edu.nju.TomatoMall.events.order.OrderShipEvent;
import cn.edu.nju.TomatoMall.models.po.Order;
import cn.edu.nju.TomatoMall.service.MessageService;
import cn.edu.nju.TomatoMall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderListener {
    private final PaymentService paymentService;
    private final MessageService messageService;

    @Autowired
    public OrderListener(PaymentService paymentService, MessageService messageService) {
        this.paymentService = paymentService;
        this.messageService = messageService;
    }

    @EventListener
    @Transactional
    public void handleOrderConfirmEvent(OrderConfirmEvent event) {
        messageService.sendNotification(
                MessageType.SHOPPING,
                event.getOrder().getUser(),
                "商家已确认订单",
                "订单: " + event.getOrder().getOrderNo() + " 已确认，等待发货",
                EntityType.ORDER,
                event.getOrder().getId(),
                MessagePriority.MEDIUM
        );
    }

    @EventListener
    @Transactional
    public void handleOrderCancelEvent(OrderCancelEvent event) {
        Order order = event.getOrder();

        // 处理退款
        if (order.getStatus() == OrderStatus.REFUND_PROCESSING) {
            paymentService.refund(order.getOrderNo(), event.getReason());
            // 通知商户订单取消
            messageService.broadcastNotificationToStore(
                    MessageType.BUSINESS,
                    order.getStore(),
                    "订单取消",
                    "订单: " + order.getOrderNo() + " 已取消\n原因: " + event.getReason(),
                    EntityType.ORDER,
                    order.getId(),
                    MessagePriority.HIGH
            );
        } else if (order.getStatus() == OrderStatus.CANCELLED) {
            if (order.getPayment().getStatus() == PaymentStatus.CANCELLED) {
                return;
            }
            // 处理支付取消
            paymentService.cancel(order.getPayment().getId());
        }
    }

    @EventListener
    @Transactional
    public void handleOrderShipEvent(OrderShipEvent event) {
        messageService.sendNotification(
                MessageType.SHOPPING,
                event.getOrder().getUser(),
                "订单已发货",
                "订单: " + event.getOrder().getOrderNo() + " 已发货\n" +
                        "物流公司: " + event.getCarrier() + "\n" +
                        "运单号: " + event.getTrackingNumber(),
                EntityType.ORDER,
                event.getOrder().getId(),
                MessagePriority.MEDIUM
        );
    }

    @EventListener
    @Transactional
    public void handleOrderDeliverEvent(OrderDeliverEvent event) {
        messageService.sendNotification(
                MessageType.SHOPPING,
                event.getOrder().getUser(),
                "订单已送达",
                "订单: " + event.getOrder().getOrderNo() + " 的商品已送达至 " + event.getDeliveryAddress() + "，请及时查收",
                EntityType.ORDER,
                event.getOrder().getId(),
                MessagePriority.MEDIUM
        );
    }
}
