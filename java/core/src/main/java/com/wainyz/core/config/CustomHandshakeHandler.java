package com.wainyz.core.config;

import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.core.pojo.domain.CustomPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Yanion_gwgzh
 */
@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomHandshakeHandler.class);
    private final JwtUtils jwtUtils;
    public CustomHandshakeHandler(@Autowired JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest servletRequest, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        logger.info("Handling handshake for request: {}", servletRequest.getURI());

        // 获取请求头中的token
        String token = Arrays.stream(servletRequest.getURI().getQuery()
                .split("&"))
                        .filter(param->param.startsWith("token="))
                .map(param->param.substring("token=".length()))
                                .findFirst()// 提取token值
                .orElse(null);
        logger.info("Token received: {}", token);

        String userId = extractUserIdFromToken(token);
        if (userId != null) {
            logger.info("User ID extracted: {}", userId);
            attributes.put(GatewayConsistent.USER_ID, userId);
            return new CustomPrincipal(userId);
        } else {
            logger.warn("Failed to extract user ID from token");
            return null;
        }
    }

    private String extractUserIdFromToken(String token) {
        try {
            // 开始检验token
            if (token == null || token.isEmpty()) {
                logger.warn("Token is null or empty");
                return null;
            }
            io.jsonwebtoken.Claims claims = jwtUtils.validate(token);
            if (claims != null) {
                // 验证通过，设置用户信息
                String userId = (String) claims.get(GatewayConsistent.USER_ID);
                logger.info("Claims validated successfully, User ID: {}", userId);
                return userId;
            }
            // token无效
            logger.warn("Token validation failed");
            return null;
        } catch (Exception e) {
            logger.error("Error validating token", e.getCause());
            return null;
        }
    }
    }