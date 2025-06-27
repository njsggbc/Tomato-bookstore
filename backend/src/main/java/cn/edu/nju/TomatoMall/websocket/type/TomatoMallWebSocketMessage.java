package cn.edu.nju.TomatoMall.websocket.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TomatoMallWebSocketMessage {
    @Getter
    public enum Type {
        CONNECTION_SUCCESS("连接成功"),
        CONNECTION_FAILED("连接失败"),
        NEW_MESSAGE("新消息"),
        UNREAD_COUNT_UPDATE("未读数量更新"),
        PING("心跳请求"),
        PONG("心跳响应"),
        USER_STATUS("用户状态"),
        SYSTEM_BROADCAST("系统广播"),
        ERROR("错误消息"),
        // 新增类型
        SERVER_SHUTDOWN("服务器关闭"),
        CONNECTION_LIMIT("连接数限制"),
        HEARTBEAT_TIMEOUT("心跳超时");

        private final String description;

        Type(String description) {
            this.description = description;
        }
    }

    private Type type;          // 消息类型
    private Object data;        // 附加数据
    private Long timestamp;     // 时间戳

    public TomatoMallWebSocketMessage(Type type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static TomatoMallWebSocketMessage success(Object data) {
        return new TomatoMallWebSocketMessage(Type.CONNECTION_SUCCESS, data);
    }

    public static TomatoMallWebSocketMessage newMessage(Object data) {
        return new TomatoMallWebSocketMessage(Type.NEW_MESSAGE, data);
    }

    public static TomatoMallWebSocketMessage unreadCountUpdate(Object data) {
        return new TomatoMallWebSocketMessage(Type.UNREAD_COUNT_UPDATE, data);
    }

    public static TomatoMallWebSocketMessage ping() {
        return new TomatoMallWebSocketMessage(Type.PING, System.currentTimeMillis());
    }

    public static TomatoMallWebSocketMessage pong() {
        return new TomatoMallWebSocketMessage(Type.PONG, System.currentTimeMillis());
    }

    public static TomatoMallWebSocketMessage error(String message) {
        return new TomatoMallWebSocketMessage(Type.ERROR, message);
    }

    public static TomatoMallWebSocketMessage serverShutdown(String message) {
        return new TomatoMallWebSocketMessage(Type.SERVER_SHUTDOWN, message);
    }

    public static TomatoMallWebSocketMessage connectionLimit(String message) {
        return new TomatoMallWebSocketMessage(Type.CONNECTION_LIMIT, message);
    }
}