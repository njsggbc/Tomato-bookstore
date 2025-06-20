package cn.edu.nju.TomatoMall.websocket;

import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import cn.edu.nju.TomatoMall.websocket.type.TomatoMallWebSocketMessage;
import cn.edu.nju.TomatoMall.websocket.type.DisconnectReason;
import cn.edu.nju.TomatoMall.websocket.type.ConnectionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TomatoMallWebSocketHandler implements WebSocketHandler {

    // 存储用户连接：userId -> Set<WebSocketSession>
    private static final ConcurrentHashMap<Integer, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    // 存储会话用户映射：WebSocketSession -> User（用于快速查找）
    private static final ConcurrentHashMap<WebSocketSession, User> sessionUserMap = new ConcurrentHashMap<>();

    // 存储连接信息：WebSocketSession -> ConnectionInfo（心跳管理）
    private static final ConcurrentHashMap<WebSocketSession, ConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityUtil securityUtil;

    // 配置参数
    @Value("${websocket.max-connections-per-user:5}")
    private int maxConnectionsPerUser;

    @Value("${websocket.heartbeat.timeout:90000}")
    private long heartbeatTimeout; // 心跳超时时间（毫秒）

    @Value("${websocket.heartbeat.max-miss:3}")
    private int maxHeartbeatMiss; // 最大心跳丢失次数

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 从URL参数获取token并验证用户身份
            User user = getUserFromSession(session);
            if (user != null) {
                // 检查连接数限制
                if (checkConnectionLimit(user, session)) {
                    addUserSession(user, session);

                    // 创建连接信息用于心跳管理
                    ConnectionInfo connectionInfo = new ConnectionInfo(session, user);
                    connectionInfoMap.put(session, connectionInfo);

                    log.info("用户 {} 建立WebSocket连接，该用户当前连接数: {}, 总在线连接数: {}",
                            user.getId(), getUserSessionCount(user.getId()), getTotalConnectionCount());

                    // 发送连接成功消息
                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", user.getId());
                    data.put("username", user.getUsername());
                    data.put("connectionTime", System.currentTimeMillis());
                    sendToSession(session, TomatoMallWebSocketMessage.success(data));
                }
            } else {
                log.warn("WebSocket连接认证失败，关闭连接");
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("认证失败"));
            }
        } catch (Exception e) {
            log.error("建立WebSocket连接失败", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 更新活跃时间
        updateConnectionActivity(session);

        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.debug("收到客户端消息: {}", payload);

            // 处理简单心跳包
            if ("PING".equals(payload)) {
                sendToSession(session, TomatoMallWebSocketMessage.pong());
                return;
            }

            // 处理JSON格式消息
            try {
                TomatoMallWebSocketMessage clientMessage = objectMapper.readValue(payload, TomatoMallWebSocketMessage.class);
                handleClientMessage(session, clientMessage);
            } catch (Exception e) {
                log.debug("非JSON格式消息，忽略处理: {}", payload);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        handleDisconnect(session, DisconnectReason.CONNECTION_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        User user = sessionUserMap.get(session);
        handleDisconnect(session, DisconnectReason.NORMAL_CLOSE);
        log.info("WebSocket连接关闭: {}, 用户: {}, 总在线连接数: {}",
                closeStatus, user != null ? user.getId() : "未知", getTotalConnectionCount());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 定时心跳检测 - 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeatCheck() {
        int timeoutCount = 0;
        List<WebSocketSession> timeoutSessions = new ArrayList<>();

        for (ConnectionInfo info : connectionInfoMap.values()) {
            if (info.isTimeout(heartbeatTimeout)) {
                timeoutSessions.add(info.getSession());
                timeoutCount++;
            }
        }

        // 处理超时连接
        for (WebSocketSession session : timeoutSessions) {
            log.warn("连接心跳超时，强制断开: 用户={}",
                    sessionUserMap.containsKey(session) ? sessionUserMap.get(session).getId() : "未知");
            handleDisconnect(session, DisconnectReason.HEARTBEAT_TIMEOUT);
        }

        if (timeoutCount > 0) {
            log.info("心跳检测完成，清理超时连接: {} 个", timeoutCount);
        }
    }

    /**
     * 定时发送心跳 - 每25秒执行一次
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        int heartbeatCount = 0;
        TomatoMallWebSocketMessage pingMessage = TomatoMallWebSocketMessage.ping();

        for (WebSocketSession session : sessionUserMap.keySet()) {
            if (session.isOpen()) {
                try {
                    sendToSession(session, pingMessage);
                    heartbeatCount++;
                } catch (Exception e) {
                    log.warn("发送心跳失败: {}", e.getMessage());
                    // 标记心跳失败
                    ConnectionInfo info = connectionInfoMap.get(session);
                    if (info != null) {
                        info.incrementHeartbeatMiss();
                        if (info.getHeartbeatMissCount() >= maxHeartbeatMiss) {
                            handleDisconnect(session, DisconnectReason.HEARTBEAT_TIMEOUT);
                        }
                    }
                }
            }
        }

        if (heartbeatCount > 0) {
            log.debug("发送心跳完成，成功发送: {} 个", heartbeatCount);
        }
    }

    /**
     * 连接统计日志 - 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void logConnectionStats() {
        int totalConnections = getTotalConnectionCount();
        int onlineUsers = getOnlineUserCount();
        double avgConnections = onlineUsers > 0 ? (double) totalConnections / onlineUsers : 0;

        log.info("WebSocket连接统计 - 在线用户: {}, 总连接数: {}",
                        onlineUsers, totalConnections);
    }

    /**
     * 优雅关闭
     */
    @PreDestroy
    public void shutdown() {
        log.info("开始关闭WebSocket服务...");

        // 通知所有客户端服务即将关闭
        TomatoMallWebSocketMessage shutdownMessage =
                TomatoMallWebSocketMessage.serverShutdown("服务器即将重启，请稍后重连");

        sessionUserMap.keySet().forEach(session -> {
            try {
                sendToSession(session, shutdownMessage);
                Thread.sleep(50); // 给客户端处理时间
                session.close(CloseStatus.GOING_AWAY.withReason("服务器关闭"));
            } catch (Exception e) {
                log.error("关闭连接失败", e);
            }
        });

        log.info("WebSocket服务关闭完成");
    }

    /**
     * 发送消息给指定用户的所有连接
     */
    public int sendToUser(Integer userId, TomatoMallWebSocketMessage message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("用户 {} 没有活跃连接", userId);
            return 0;
        }

        int successCount = 0;
        Iterator<WebSocketSession> iterator = sessions.iterator();

        while (iterator.hasNext()) {
            WebSocketSession session = iterator.next();
            if (session.isOpen()) {
                try {
                    sendToSession(session, message);
                    successCount++;
                } catch (Exception e) {
                    log.error("发送消息到会话失败，用户ID: {}", userId, e);
                    // 移除失效连接
                    iterator.remove();
                    cleanupConnection(session);
                }
            } else {
                // 移除已关闭连接
                iterator.remove();
                cleanupConnection(session);
            }
        }

        // 如果用户所有连接都失效，移除用户记录
        if (sessions.isEmpty()) {
            userSessions.remove(userId);
        }

        log.debug("向用户 {} 发送消息，成功发送到 {} 个连接", userId, successCount);
        return successCount;
    }

    /**
     * 批量发送消息给多个用户
     */
    public int sendToUsers(List<Integer> userIds, TomatoMallWebSocketMessage message) {
        int totalSuccessCount = 0;
        for (Integer userId : userIds) {
            totalSuccessCount += sendToUser(userId, message);
        }
        log.debug("批量发送消息给 {} 个用户，成功发送到 {} 个连接", userIds.size(), totalSuccessCount);
        return totalSuccessCount;
    }

    /**
     * 广播消息给所有在线用户
     */
    public int broadcastToAll(TomatoMallWebSocketMessage message) {
        return sendToUsers(getOnlineUserIds(), message);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Integer userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        return sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    /**
     * 获取用户连接数
     */
    public int getUserSessionCount(Integer userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions == null ? 0 : (int) sessions.stream().filter(WebSocketSession::isOpen).count();
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnectionCount() {
        return sessionUserMap.size();
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID
     */
    public List<Integer> getOnlineUserIds() {
        return new ArrayList<>(userSessions.keySet());
    }

    // ====================== 私有方法 ======================

    /**
     * 检查连接数限制
     */
    private boolean checkConnectionLimit(User user, WebSocketSession session) throws IOException {
        Set<WebSocketSession> existingSessions = userSessions.get(user.getId());
        if (existingSessions != null && existingSessions.size() >= maxConnectionsPerUser) {
            log.warn("用户 {} 连接数超限，当前连接数: {}, 最大允许: {}",
                    user.getId(), existingSessions.size(), maxConnectionsPerUser);

            // 关闭最老的连接
            WebSocketSession oldestSession = existingSessions.iterator().next();
            sendToSession(oldestSession, TomatoMallWebSocketMessage.connectionLimit("连接数超限，已断开旧连接"));
            oldestSession.close(CloseStatus.NORMAL.withReason("连接数超限"));
            cleanupConnection(oldestSession);
        }
        return true;
    }

    /**
     * 添加用户连接
     */
    private void addUserSession(User user, WebSocketSession session) {
        userSessions.computeIfAbsent(user.getId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        sessionUserMap.put(session, user);
    }

    /**
     * 处理断连
     */
    private void handleDisconnect(WebSocketSession session, DisconnectReason reason) {
        User user = sessionUserMap.get(session);
        cleanupConnection(session);

        if (user != null) {
            log.debug("用户 {} 断开连接，原因: {}", user.getId(), reason.getDescription());
        }
    }

    /**
     * 清理连接
     */
    private void cleanupConnection(WebSocketSession session) {
        User user = sessionUserMap.remove(session);
        connectionInfoMap.remove(session);

        if (user != null) {
            Set<WebSocketSession> sessions = userSessions.get(user.getId());
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(user.getId());
                }
            }
        }
    }

    /**
     * 更新连接活跃时间
     */
    private void updateConnectionActivity(WebSocketSession session) {
        ConnectionInfo info = connectionInfoMap.get(session);
        if (info != null) {
            info.updateActiveTime();
        }
    }

    /**
     * 发送消息到指定会话
     */
    private void sendToSession(WebSocketSession session, Object message) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(message);
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    /**
     * 从会话中获取用户信息
     */
    private User getUserFromSession(WebSocketSession session) {
        try {
            String token = getTokenFromSession(session);
            if (token != null) {
                if (securityUtil.verifyToken(token)) {
                    return securityUtil.getUser(token);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("从会话中获取用户信息失败", e);
            return null;
        }
    }

    /**
     * 从会话中提取token
     */
    private String getTokenFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null && query.contains("token=")) {
                    return extractTokenFromQuery(query);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("从会话中提取token失败", e);
            return null;
        }
    }

    /**
     * 从查询参数中提取token
     */
    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    /**
     * 处理客户端消息
     */
    private void handleClientMessage(WebSocketSession session, TomatoMallWebSocketMessage message) {
        User user = sessionUserMap.get(session);
        if (user == null) {
            return;
        }

        TomatoMallWebSocketMessage.Type messageType = message.getType();
        if (messageType == null) {
            return;
        }

        switch (messageType) {
            case PING:
                try {
                    sendToSession(session, TomatoMallWebSocketMessage.pong());
                } catch (IOException e) {
                    log.error("发送心跳响应失败", e);
                }
                break;
            case PONG:
                // 客户端响应心跳，更新活跃时间
                updateConnectionActivity(session);
                log.debug("收到用户 {} 的心跳响应", user.getId());
                break;
            case USER_STATUS:
                // 处理用户状态更新
                log.debug("用户 {} 状态更新: {}", user.getId(), message.getData());
                break;
            default:
                log.debug("收到未知类型消息: {}", messageType);
                break;
        }
    }
}