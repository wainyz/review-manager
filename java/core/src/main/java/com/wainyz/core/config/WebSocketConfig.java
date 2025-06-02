package com.wainyz.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * - 全局通知 /all/notice

 * @author Yanion_gwgzh
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    public WebSocketConfig(@Autowired CustomHandshakeHandler customHandshakeHandler) {
        this.customHandshakeHandler = customHandshakeHandler;
    }
    private final CustomHandshakeHandler customHandshakeHandler;
    public static final String ALL_NOTICE_PREFIX="/all";
    public static final String ALL_NOTICE = ALL_NOTICE_PREFIX + "/notice";

    public static final String WEBSOCKET_URI="/websocket";
    public static final String APP_PREFIX = "/ws";
    public static final String USER_NOTICE_PREFIX ="/user";



    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个名为"websocket"的endpoint，并开启SockJS备份选项。

        registry.addEndpoint(WEBSOCKET_URI)
                .setHandshakeHandler(customHandshakeHandler)
                .setAllowedOrigins("http://localhost:80", "https://wainyz.online", "https://www.wetools.com","http://localhost:5173")
                ;
                //.withSockJS();
        logger.info("[握手处理器 非空检查] {}", customHandshakeHandler);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes(APP_PREFIX);
        //config.setUserDestinationPrefix(USER_NOTICE_PREFIX);
        // 启用简单的内存中的消息代理（broker）
        config.enableSimpleBroker(ALL_NOTICE_PREFIX,USER_NOTICE_PREFIX);
    }

}
