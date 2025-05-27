package cn.edu.nju.TomatoMall.websocket.handler;

import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.service.MessageService;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import cn.edu.nju.TomatoMall.websocket.message.TomatoMallWebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 从URL参数获取token并验证用户身份
            User user = getUserFromSession(session);
            if (user != null) {
                addUserSession(user, session);
                log.info("用户 {} 建立WebSocket连接，该用户当前连接数: {}, 总在线连接数: {}",
                        user.getId(), getUserSessionCount(user.getId()), getTotalConnectionCount());

                // 发送连接成功消息
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("username", user.getUsername());
                sendToSession(session, TomatoMallWebSocketMessage.success(data));
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
    public void handleMessage(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.debug("收到客户端消息: {}", payload);

            // 处理心跳包
            if ("PING".equals(payload)) {
                sendToSession(session, TomatoMallWebSocketMessage.pong());
                return;
            }

            // 可以处理其他客户端消息
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
        removeUserSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        User user = sessionUserMap.get(session);
        removeUserSession(session);
        log.info("WebSocket连接关闭: {}, 用户: {}, 总在线连接数: {}",
                closeStatus, user != null ? user.getId() : "未知", getTotalConnectionCount());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 发送消息给指定用户的所有连接
     * @param userId 用户ID
     * @param message 消息对象
     * @return 成功发送的连接数
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
                    sessionUserMap.remove(session);
                }
            } else {
                // 移除已关闭连接
                iterator.remove();
                sessionUserMap.remove(session);
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
     * @param userIds 用户ID列表
     * @param message 消息对象
     * @return 成功发送的总连接数
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
     * @param message 消息对象
     * @return 成功发送的连接数
     */
    public int broadcastToAll(TomatoMallWebSocketMessage message) {
        return sendToUsers(getOnlineUserIds(), message);
    }

    /**
     * 检查用户是否在线（至少有一个连接）
     * @param userId 用户ID
     * @return 是否在线
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
     * @param userId 用户ID
     * @return 连接数
     */
    public int getUserSessionCount(Integer userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions == null ? 0 : (int) sessions.stream().filter(WebSocketSession::isOpen).count();
    }

    /**
     * 获取总连接数
     * @return 总连接数
     */
    public int getTotalConnectionCount() {
        return sessionUserMap.size();
    }

    /**
     * 获取在线用户数量
     * @return 在线用户数
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID
     * @return 在线用户ID列表
     */
    public List<Integer> getOnlineUserIds() {
        return new ArrayList<>(userSessions.keySet());
    }

    /**
     * 断开用户所有连接
     * @param userId 用户ID
     * @param reason 断开原因
     */
    public void disconnectUser(Integer userId, String reason) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.close(CloseStatus.NORMAL.withReason(reason));
                    }
                } catch (IOException e) {
                    log.error("断开用户连接失败，用户ID: {}", userId, e);
                }
            }
            userSessions.remove(userId);
            // 清理会话映射
            for (WebSocketSession session : sessions) {
                sessionUserMap.remove(session);
            }
        }
    }

    // 私有方法

    /**
     * 添加用户连接
     */
    private void addUserSession(User user, WebSocketSession session) {
        userSessions.computeIfAbsent(user.getId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        sessionUserMap.put(session, user);
    }

    /**
     * 移除用户连接
     */
    private void removeUserSession(WebSocketSession session) {
        User user = sessionUserMap.remove(session);
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
                // 验证token
                if (securityUtil.verifyToken(token)) {
                    // 获取用户信息
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