package com.wainyz.core.utils;


import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.core.manager.QuickStatusManager;
import com.wainyz.core.pojo.domin.DeepSeekRequestDO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * &#064;description:  将deepseek调用请求发送到rabbitMq中，等待python代码来实现具体的调用。
 * @author Yanion_gwgzh
 */
@Component
public class MessageSender {
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    private QuickStatusManager quickStatusManager;

    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

/**
 * 发送deepseek调用细节到rabbitMq中
 *
 * @return
 */
    public boolean sendDeepSeekRequest(DeepSeekRequestDO requestDO) {
        // 设置messageId,作为去重的标识
        String messageId = requestDO.getUserId()+":"+requestDO.getFileId();
        rabbitTemplate.convertAndSend(RabbitMqConsistent.EXCHANGE_NAME, RabbitMqConsistent.ROUTING_KEY, requestDO, message -> {
            message.getMessageProperties().setMessageId(messageId);
            return message;
        });
        if (quickStatusManager.shouldBeginRecordSend()){
            quickStatusManager.sendRecord(messageId);
        }
        return true;
    }
}
