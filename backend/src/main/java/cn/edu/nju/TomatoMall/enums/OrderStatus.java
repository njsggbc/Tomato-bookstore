package cn.edu.nju.TomatoMall.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum OrderStatus {
    // 核心流程状态（非终态）
    AWAITING_PAYMENT("待支付"),
    PROCESSING("订单处理中"),
    AWAITING_SHIPMENT("待发货"),
    IN_TRANSIT("运输中"),
    AWAITING_RECEIPT("待收货"),

    // TODO: 售后流程状态（非终态）
    RETURN_AUDIT("退货审核中"),
    RETURN_IN_PROGRESS("退货处理中"),
    INSPECTION_PENDING("商品待验收"),
    REFUND_PROCESSING("退款处理中"),
    AFTER_SALE_FAILURE("售后失败"),
    DISPUTE_UNDER_REVIEW("售后争议处理中"),

    // 终态（流程结束）
    COMPLETED("订单已完成"),
    CANCELLED("订单已取消"),
    CLOSED("订单已关闭");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public static List<OrderStatus> afterSaleStatus() {
        return Arrays.asList(RETURN_AUDIT, RETURN_IN_PROGRESS, INSPECTION_PENDING, REFUND_PROCESSING, AFTER_SALE_FAILURE, DISPUTE_UNDER_REVIEW);
    }

    public static List<OrderStatus> coreStatus() {
        return Arrays.asList(AWAITING_PAYMENT, PROCESSING, AWAITING_SHIPMENT, IN_TRANSIT, AWAITING_RECEIPT);
    }

    public static List<OrderStatus> terminalStatus() {
        return Arrays.asList(COMPLETED, CANCELLED, CLOSED);
    }

}
