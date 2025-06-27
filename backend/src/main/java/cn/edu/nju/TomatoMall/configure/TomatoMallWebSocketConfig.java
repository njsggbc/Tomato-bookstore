package cn.edu.nju.TomatoMall.configure;

import cn.edu.nju.TomatoMall.websocket.TomatoMallWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class TomatoMallWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TomatoMallWebSocketHandler messageWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("*");  // 生产环境需要配置具体域名
    }
}