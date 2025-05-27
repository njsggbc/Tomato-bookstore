package cn.edu.nju.TomatoMall.websocket.config;

import cn.edu.nju.TomatoMall.websocket.handler.TomatoMallWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class TomatoMallWebSocketConfigurer implements WebSocketConfigurer {

    @Autowired
    private TomatoMallWebSocketHandler messageWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("*")  // 生产环境需要配置具体域名
                .withSockJS();  // 支持SockJS降级
    }
}