package cn.edu.nju.TomatoMall.websocket.type;

import cn.edu.nju.TomatoMall.models.po.User;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class ConnectionInfo {
    private final WebSocketSession session;
    private final User user;
    private final long createTime;
    private volatile long lastActiveTime;
    private volatile int heartbeatMissCount;

    public ConnectionInfo(WebSocketSession session, User user) {
        this.session = session;
        this.user = user;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = System.currentTimeMillis();
        this.heartbeatMissCount = 0;
    }

    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
        this.heartbeatMissCount = 0;
    }

    public void incrementHeartbeatMiss() {
        this.heartbeatMissCount++;
    }

    public boolean isTimeout(long timeoutMillis) {
        return System.currentTimeMillis() - lastActiveTime > timeoutMillis;
    }

    public long getConnectionDuration() {
        return System.currentTimeMillis() - createTime;
    }

}
