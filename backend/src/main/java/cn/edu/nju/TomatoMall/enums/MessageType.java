package cn.edu.nju.TomatoMall.enums;

import java.util.Arrays;
import java.util.List;

public enum MessageType {
    BROADCAST, // 公告
    SYSTEM, // 系统通知
    BUSINESS, // 业务相关
    SHOPPING, // 购物相关
    CHAT, // 私聊
    GROUP_CHAT; // 群聊

    public static boolean isNotification(MessageType type) {
        return type == SYSTEM || type == BUSINESS || type == SHOPPING;
    }

    public static boolean isChat(MessageType type) {
        return type == CHAT || type == GROUP_CHAT;
    }

    public static List<MessageType> getNotificationTypes() {
        return Arrays.asList(SYSTEM, BUSINESS, SHOPPING);
    }

    public static List<MessageType> getChatTypes() {
        return Arrays.asList(CHAT, GROUP_CHAT);
    }
}
