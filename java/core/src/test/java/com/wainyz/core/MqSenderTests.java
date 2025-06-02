package com.wainyz.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wainyz.core.pojo.domain.DeepSeekRequestDO;
import com.wainyz.core.utils.MessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class MqSenderTests {
    /**
     * 测试消息发送,需要到pycharm中查看消息是否处理
     */
    @Autowired
    private MessageSender messageSender;
    @Test
    public void testSend() throws JsonProcessingException {
        assert messageSender != null;
        DeepSeekRequestDO deepSeekRequestDO = new DeepSeekRequestDO();
        deepSeekRequestDO.setFileId("1");
        deepSeekRequestDO.setUserId("1");
        deepSeekRequestDO.setSystemPrompt("1");
        deepSeekRequestDO.setUserContent("1");
        messageSender.sendDeepSeekRequest(deepSeekRequestDO);
    }
}
