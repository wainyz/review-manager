package com.wainyz.core.service;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于发送让前端刷新请求
 * @author Yanion_gwgzh
 */
public class MyWebSocketHandler extends TextWebSocketHandler {
    //------------------------0---------------------
    /**
     * 存储会话
     */
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 存储会话
        String userId = session.getAttributes().get("userId").toString();
        sessions.put(userId, session);
        System.out.println("[23]New session established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("[29]Received message: " + payload);
        // 回显消息
        session.sendMessage(new PingMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除会话
        sessions.remove(session.getId());
        System.out.println("[39]Session closed: " + session.getId());
    }
    private WebSocketSession getSession(String userId) {
        return sessions.get(userId);
    }
    //========================0========================

    /**
     * 发送消息到所有会话
     * @param message
     */
     public void sendMessageToAll(String message) {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 提醒指定会话
     * @param userId
     */
    public void sendMessageToSession(String userId) {
        WebSocketSession session = getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                // 发送消息到会话，客户端收到就自动刷新。
                session.sendMessage(new PingMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //========================1========================
}