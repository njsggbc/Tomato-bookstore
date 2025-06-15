package cn.edu.nju.TomatoMall.service.impl.listener;

import cn.edu.nju.TomatoMall.enums.*;
import cn.edu.nju.TomatoMall.models.po.Payment;
import cn.edu.nju.TomatoMall.service.AdvertisementService;
import cn.edu.nju.TomatoMall.service.MessageService;
import cn.edu.nju.TomatoMall.service.OrderService;
import cn.edu.nju.TomatoMall.service.PaymentService;
import cn.edu.nju.TomatoMall.service.impl.events.payment.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentListener {

    private final OrderService orderService;
    private final MessageService messageService;
    private final PaymentService paymentService;
    private final AdvertisementService advertisementService;

    @Autowired
    public PaymentListener(OrderService orderService,
                           MessageService messageService,
                           PaymentService paymentService, AdvertisementService advertisementService) {
        this.orderService = orderService;
        this.messageService = messageService;
        this.paymentService = paymentService;
        this.advertisementService = advertisementService;
    }

    /**
     * 处理支付创建事件
     * 调用支付服务进行支付超时处理
     *
     * @param payment 支付信息
     */
    @EventListener
    @Transactional
    public void handlePaymentCreated(Payment payment) {
        paymentService.schedulePaymentTimeout(payment);
    }

    /**
     * 处理支付成功事件
     * 更新订单状态为处理中
     *
     * @param event 支付成功事件
     */
    @EventListener
    @Transactional
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        Payment payment = event.getPayment();
        paymentService.removeSchedulePaymentTimeout(payment);

        payment.getOrders().forEach(order -> {
            orderService.updateStatus(
                    order,
                    order.getUser(),
                    OrderEvent.PAY,
                    OrderStatus.PROCESSING,
                    "支付成功，交易号：" + payment.getTradeNo()
            );
        });

        // 通知用户支付成功
        messageService.sendNotification(
                MessageType.SHOPPING,
                payment.getUser(),
                "支付成功",
                "您的支付已成功，交易号：" + payment.getTradeNo(),
                EntityType.PAYMENT,
                payment.getId(),
                MessagePriority.MEDIUM
        );

        // 通知商户有订单待处理
        payment.getOrders().forEach(order -> {
            messageService.broadcastNotificationToStore(
                    MessageType.BUSINESS,
                    order.getStore(),
                    "新订单",
                    "您有新的订单待处理，请尽快查看。",
                    EntityType.ORDER,
                    order.getId(),
                    MessagePriority.HIGH
            );
        });
    }

    @EventListener
    @Transactional
    public void handlePaymentFailEvent(PaymentFailEvent event) {
        messageService.sendNotification(
                MessageType.SHOPPING,
                event.getPayment().getUser(),
                "支付失败",
                "您的支付交易失败，请检查支付信息或联系客服，" +
                        "交易号：" + event.getPayment().getTradeNo(),
                EntityType.PAYMENT,
                event.getPayment().getId(),
                MessagePriority.HIGH
        );
    }

    /**
     * 处理支付取消事件
     * 释放库存并更新订单状态为已取消
     *
     * @param event 支付取消事件
     */
    @EventListener
    @Transactional
    public void handlePaymentCancelEvent(PaymentCancelEvent event) {
        Payment payment = event.getPayment();
        paymentService.removeSchedulePaymentTimeout(payment);

        // 处理订单
        payment.getOrders().forEach(order -> {
            orderService.cancelInternal(order.getId(), event.getReason());
        });

        // 处理其他实体类型
        switch (payment.getEntityType()) {
            case ADVERTISEMENT_PLACEMENT:
                advertisementService.cancelDeliverAdvertisementInternal(payment.getId());
                break;
            default:
                break;
        }
    }

    /**
     * 处理订单退款成功事件
     * 更新订单状态为已取消
     *
     * @param event 订单退款成功事件
     */
    @EventListener
    @Transactional
    public void handleRefundSuccessEvent(RefundSuccessEvent event) {
        if (event.getOrder() != null) {
            orderService.updateStatus(
                    event.getOrder(),
                    null,
                    OrderEvent.REFUND,
                    OrderStatus.CANCELLED,
                    "已退款: " + event.getRefundAmount() + "\n交易号：" + event.getTradeNo()
            );
        }

        // 通知用户退款成功
        messageService.sendNotification(
                MessageType.SHOPPING,
                event.getPayment().getUser(),
                "退款成功",
                "您的订单已成功退款，金额：" + event.getRefundAmount() + "，交易号：" + event.getTradeNo(),
                event.getOrder() == null ? EntityType.PAYMENT : EntityType.ORDER,
                event.getOrder() == null ? event.getPayment().getId() : event.getOrder().getId(),
                MessagePriority.MEDIUM
        );
    }

    @EventListener
    @Transactional
    public void handleRefundFailEvent(RefundFailEvent event) {
        messageService.sendNotification(
                MessageType.SHOPPING,
                event.getPayment().getUser(),
                "退款失败",
                "您的退款请求失败，请联系客服处理，" +
                        "支付单号：" + event.getPayment().getPaymentNo() + "\n" +
                        "退款金额：" + event.getRefundAmount() + "\n" +
                        "交易号：" + event.getTradeNo(),
                EntityType.PAYMENT,
                event.getPayment().getId(),
                MessagePriority.HIGH
        );
    }
}
