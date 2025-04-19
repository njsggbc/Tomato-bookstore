package cn.edu.nju.TomatoMall.enums;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    /**
     * 等待支付
     */
    PENDING,

    /**
     * 支付成功
     */
    SUCCESS,

    /**
     * 支付失败
     */
    FAILED,

    /**
     * 已退款
     */
    REFUNDED,

    /**
     * 部分退款
     */
    PARTIALLY_REFUNDED,

    /**
     * 支付超时
     */
    TIMEOUT,

    /**
     * 已取消
     */
    CANCELLED
}
