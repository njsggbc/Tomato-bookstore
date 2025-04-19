package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.OrderStatus;
import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.models.po.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class StoreOrderInfoResponse {
    @NonNull
    private int orderId;

    @NonNull
    private String orderNo;

    @NonNull
    private int customerId;

    @NonNull
    private String customerUsername;

    @NonNull
    private String customerPhone;

    @NonNull
    private OrderStatus status;

    @NonNull
    private List<OrderItemInfoResponse> items;

    @NonNull
    private BigDecimal totalPrice;

    @NonNull
    private String createTime;

    @NonNull
    private List<OrderLogResponse> logs;

    @NonNull
    private List<ShippingInfoResponse> shippingInfo;

    private String remark;
    private String paymentId; // 支付单号，对应于购物平台的合并支付单号
    private PaymentMethod paymentMethod;
    private String tradeNo; // 交易单号，对应于支付平台，如支付宝交易号

    public StoreOrderInfoResponse(Order order) {
        this.orderId = order.getId();
        this.orderNo = order.getOrderNo();
        this.customerId = order.getUser().getId();
        this.customerUsername = order.getUser().getUsername();
        this.customerPhone = order.getUser().getPhone();
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
            this.tradeNo = order.getPayment().getId();
        }
    }

}
