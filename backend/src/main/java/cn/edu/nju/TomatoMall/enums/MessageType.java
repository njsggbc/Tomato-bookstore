package cn.edu.nju.TomatoMall.enums;

public enum MessageType {
    BROADCAST, // 广播通知
    SYSTEM, // 系统通知
    BUSINESS, // 业务相关
    SHOPPING, // 购物相关
    CHAT;// 私聊消息

    public static boolean isNotification(MessageType type) {
        return type != CHAT;
    }
}
