package com.campus.equipment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理前缀
        // /topic 用于广播消息（订阅模式）
        // /queue 用于点对点消息
        registry.enableSimpleBroker("/topic", "/queue");
        // 配置应用目的地前缀（客户端发送消息时使用）
        registry.setApplicationDestinationPrefixes("/app");
        // 配置用户目的地前缀（用于点对点消息）
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点，客户端通过该端点连接 WebSocket
        // withSockJS() 提供 SockJS 降级方案（浏览器不支持 WebSocket 时回退）
        registry.addEndpoint("/ws/equipment")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 也注册不带 SockJS 的 WebSocket 端点
        registry.addEndpoint("/ws/equipment")
                .setAllowedOriginPatterns("*");
    }
}
