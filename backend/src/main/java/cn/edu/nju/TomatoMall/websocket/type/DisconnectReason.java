package cn.edu.nju.TomatoMall.websocket.type;

import lombok.Getter;

@Getter
public enum DisconnectReason {
    NORMAL_CLOSE("正常关闭"),
    HEARTBEAT_TIMEOUT("心跳超时"),
    AUTHENTICATION_FAILED("认证失败"),
    CONNECTION_ERROR("连接错误"),
    SERVER_SHUTDOWN("服务器关闭"),
    MAX_CONNECTIONS_EXCEEDED("超出最大连接数");

    private final String description;

    DisconnectReason(String description) {
        this.description = description;
    }

}