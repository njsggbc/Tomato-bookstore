package cn.edu.nju.TomatoMall.enums;

public enum OrderEvent {
    // 核心流程事件
    CREATE,            // 创建订单
    PAY,               // 发起支付（或支付成功）
    EXPIRE,            // 支付超时
    CONFIRM,           // 商家确认订单
    REFUSE,            // 商家拒绝订单
    SHIP,              // 发货
    DELIVER,           // 签收（物流送达）
    CONFIRM_RECEIPT,   // 用户确认收货
    CANCEL,            // 取消订单
    CLOSE,             // 关闭订单（终止）

    // TODO: 售后流程事件
    REQUEST_RETURN,    // 发起退货
    CANCEL_RETURN,     // 取消退货
    APPROVE_RETURN,    // 同意退货
    REFUSE_RETURN,     // 拒绝退货
    SEND_RETURN,       // 用户寄回退货商品
    REFUND,            // 发起退款
    COMPLETE_REFUND,   // 退款完成
    RAISE_DISPUTE,     // 发起争议
    CANCEL_DISPUTE,    // 取消争议
    RESOLVE_DISPUTE    // 争议解决
}

