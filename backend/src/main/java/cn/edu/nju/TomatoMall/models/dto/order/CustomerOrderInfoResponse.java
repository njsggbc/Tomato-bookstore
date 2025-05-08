package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Data;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CustomerOrderInfoResponse {
    private int orderId;
    private String orderNo;
    private int storeId;
    private String storeName;
    private OrderStatus status;
    private List<OrderItemInfoResponse> items;
    private BigDecimal totalPrice;
    private String createTime;
    private List<OrderLogResponse> logs;
    private List<ShippingInfoResponse> shippingInfo;
    private String remark;
    private int paymentId;
    private PaymentMethod paymentMethod;
    private String paymentNo; // 支付单号，对应于购物平台的合并支付单号
    private String tradeNo; // 交易单号，对应于支付平台，如支付宝交易号

    public CustomerOrderInfoResponse(Order order) {
        this.orderId = order.getId();
        this.orderNo = order.getOrderNo();
        this.storeId = order.getStore().getId();
        this.storeName = order.getStore().getName();
        this.status = order.getStatus();
        this.items = order.getItems().stream()
                .map(OrderItemInfoResponse::new)
                .collect(Collectors.toList());
        this.totalPrice = order.getTotalAmount();
        this.createTime = order.getCreateTime().toString();
        this.logs = order.getLogs().stream()
                .map(OrderLogResponse::new)
                .collect(Collectors.toList());
        this.shippingInfo = order.getShippingInfos().stream()
                .map(ShippingInfoResponse::new)
                .collect(Collectors.toList());
        this.remark = order.getRemark();

        if (order.getPayment() != null) {
            this.paymentId = order.getPayment().getId();
            this.paymentMethod = order.getPayment().getPaymentMethod();
            this.paymentNo = order.getPayment().getPaymentNo();
            this.tradeNo = order.getPayment().getTradeNo();
        }
    }
}
