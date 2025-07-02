package com.wainyz.core.spring_ai;

import com.rabbitmq.client.Channel;
import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.core.pojo.domain.DeepSeekRequestDO;
import com.wainyz.core.pojo.domain.DeepSeekResponse;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * 每个对象都可以监听并处理RabbitMq上队列的消息
 * @author Yanion_gwgzh
 */
@EnableRabbit
public class RabbitMqListener {

    @RabbitListener(
            queues = RabbitMqConsistent.QUEUE_NAME,
            ackMode = "MANUAL"
    )
    public void handleGenerateQuestionResponseMessage(
            @Payload DeepSeekRequestDO message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel
    ) {
            DeepSeekResponse response = SpringAiManager.useAi(message);
            //TODO: 待完成
    }
}
