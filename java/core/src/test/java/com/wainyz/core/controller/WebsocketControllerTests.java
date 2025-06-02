package com.wainyz.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.core.config.WebSocketConfig;
import com.wainyz.core.pojo.domain.LastRead;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.LastReadService;
import com.wainyz.core.service.NoticeService;
import com.wainyz.core.service.WebSocketMessageService;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class WebsocketControllerTests {
    @PostConstruct
    public void init(){
        //CoreApplication.main(new String[]{});
    }
    @Autowired
    private WebsocketController websocketController;
    @Autowired
    private WebSocketMessageService webSocketMessageService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private LastReadService lastReadService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtils jwtUtils;
    private final Logger logger = LoggerFactory.getLogger(WebsocketControllerTests.class);
    String receivedMessage;
    private final CountDownLatch latch = new CountDownLatch(1);
    @Value("${server.port}")
    String port;
    @Test
    public void testStompMessaging() throws Exception {
        // 1. 创建 STOMP 客户端
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new StringMessageConverter());

        // 2. 连接服务端端点
        String websocketUrl = "ws://localhost:"+port+WebSocketConfig.WEBSOCKET_URI; // 根据实际端口调整
        StompSession session = stompClient.connectAsync(websocketUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        // 3. 订阅目标地址（/topic/greetings）
        BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
        session.subscribe("/ws/hello", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                responseQueue.add((String) payload);
            }
        });

        // 4. 发送消息到服务端（/app/hello）
        String message = "Test Message";
        session.send(WebSocketConfig.APP_PREFIX+"hello", message);

        // 5. 验证是否收到预期回复
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String response = responseQueue.poll();
            assertEquals("服务端回复: " + message, response);
        });

        // 关闭连接
        session.disconnect();
    }

    /**
     * 测试建立连接
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    @Test
    public void testWebSocketClient() throws URISyntaxException, InterruptedException {
        String url = "ws://localhost:8003" + WebSocketConfig.WEBSOCKET_URI;
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> testData = new HashMap<>();
        testData.put(GatewayConsistent.USER_ID,"1");
        testData.put(GatewayConsistent.USER_EMAIL,"player_simple@163.com");
        headers.put("Authorization", jwtUtils.generateToken(new DefaultClaims(testData)));



        WebSocketClient client = new WebSocketClient(new URI(url),headers) {


            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("[system] 建立websocket连接");
                send("Test Message");
            }

            @Override
            public void onMessage(String message) {
                receivedMessage = message;
                latch.countDown();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("[system] 关闭连接...");
                System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        client.connect();

        if (latch.await(3, TimeUnit.SECONDS)) {
            assertNotNull(receivedMessage);
            System.out.println("[system] 收到 message: " + receivedMessage);
        } else {
            throw new RuntimeException("Timeout while waiting for the message");
        }
    }

    /**
     * 测试发送全局消息
     */
    @Test
    public void testSendAllNotice() throws JsonProcessingException, SQLException {
        String testContent = "_$测试数据 " + System.currentTimeMillis();
        webSocketMessageService.noticeAllUsers(testContent);
        List<Notice> list = noticeService.list();
        assert !list.isEmpty();
        String s = objectMapper.writeValueAsString(list);
        logger.info(s);
        assert s.contains(testContent);
        //取消测试数据
        Notice notice = objectMapper.readValue(s, Notice.class);
        noticeService.removeById(notice.getId());
    }
    /**
     * 测试获取未读消息
     * 测试数据 ：
     *  notice数据一条 2025年 一条 2023年id":1
     *  lastRead时间 userid = 1, 2024 年 这样可以看到后面的消息看不到前面的消息
     *
     */
    @Test
    public void testGetUnreadNotices() throws JsonProcessingException {
        lastReadService.removeById(1);
        LastRead lastRead = new LastRead();
        lastRead.setId(1L);
        lastRead.setTimestamp(Date.from(LocalDateTime.of(2024,1,1,1,1,1).toInstant(ZoneOffset.UTC)));
        lastReadService.save(lastRead);
        // 存储两条notice，2023 id=1和2025的id=2
        Notice notice1 = Notice.getTestObject(String.valueOf(1L));
        notice1.setTimestamp(Date.from(LocalDateTime.of(2023,1,1,1,1,1).toInstant(ZoneOffset.UTC)));
        Notice notice2 = Notice.getTestObject(String.valueOf(2L));
        notice2.setTimestamp(Date.from(LocalDateTime.of(2025,1,1,1,1,1).toInstant(ZoneOffset.UTC)));
        noticeService.removeById(1);
        noticeService.removeById(2);
        noticeService.save(notice1);
        noticeService.save(notice2);
        ReturnModel unreadNotices = websocketController.getUnreadNotices(1L);
        logger.info((String) unreadNotices.getData());
        // 测试是否不包含过去的notice 即 id = 1的记录。
        assert !((String) unreadNotices.getData()).contains(":1,");
        // 测试是否包含过去的notice 即 id = 2的记录。
        assert  ((String) unreadNotices.getData()).contains(":2,");
    }
}
