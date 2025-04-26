package com.wainyz.core.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.core.analyizer.QuestionFileAnalyzer;
import com.wainyz.core.analyizer.ReviewAnalyzer;
import com.wainyz.core.analyizer.ScoringRecordAnalyzer;
import com.wainyz.core.manager.DataFileManager;
import com.wainyz.core.manager.QuickStatusManager;
import com.wainyz.core.pojo.domin.DeepSeekResponse;
import com.wainyz.core.pojo.domin.HistoryFileDO;
import com.wainyz.core.pojo.domin.QuestionFileDO;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Yanion_gwgzh
 */
@Component
@EnableRabbit
public class RabbitMQConsumer {
    @Autowired
    private ReviewAnalyzer reviewAnalyzer;
    @Autowired
    private QuickStatusManager quickStatusManager;
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
//-----------------------------1--------------------
    /**
     * 处理生成试题的响应消息
     * @param message
     * @param deliveryTag
     * @param channel
     * @throws IOException
     */
    @RabbitListener(
        queues = RabbitMqConsistent.DEEPSEEK_RESPONSE_QUEUE,
        ackMode = "MANUAL"
    )
    public void handleGenerateQuestionResponseMessage(
        @Payload DeepSeekResponse message,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        Channel channel
    ) throws IOException {
        try {
            // 业务处理逻辑 #需要检测是否得到正确的userId和fileId#
            processGenerateQuestionMessage(message);
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            // 设置状态
            quickStatusManager.setStatus(message.getUserId());
            if(quickStatusManager.shouldBeginRecordReceiver()){
                quickStatusManager.receiverRecord(message.getUserId()+":"+message.getUserId());
            }
        } catch (Exception e) {
            // 系统异常处理
            logger.error("系统异常: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            // 直接丢弃
        }
    }

    /**
     * 处理消息的具体逻辑
     * @param message
     */

    private void processGenerateQuestionMessage(DeepSeekResponse message) {
        // 实现具体业务逻辑
        logger.info("Processing message: {}", message);
        try {
            reviewAnalyzer.analyzeGenerateQuestionResponse(message.getId(), message);
        } catch (Exception e) {
            logger.error("处理Mq消息出现 异常："+ e);
            throw new RuntimeException(e);
        }
    }
//============================1===========================
//-----------------------2---------------------
    @Autowired
    QuestionFileAnalyzer questionFileAnalyzer;
    /**
     * 处理打分的响应消息
     * @param message
     * @param deliveryTag
     * @param channel
     */
    @RabbitListener(
            queues = RabbitMqConsistent.DEEPSEEK_SCORING_RESPONSE_QUEUE,
            ackMode = "MANUAL"
    )
    public void handleScoringResponseMessage(
            @Payload DeepSeekResponse message,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel
    ) throws InterruptedException, IOException {
        try {
            // 业务处理逻辑
            processScoringMessage(message);
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            // 设置状态
            quickStatusManager.setStatus(message.getUserId());
            if(quickStatusManager.shouldBeginRecordReceiver()){
                quickStatusManager.receiverRecord(message.getUserId()+":"+message.getUserId());
            }
        } catch (Exception e) {
            // 系统异常处理
            logger.error("系统异常: {}", e.getMessage());
            Thread.sleep(1, TimeUnit.SECONDS.ordinal());
            channel.basicNack(deliveryTag, false, false );
            // 重新入队
        }
    }
    /**
     * 处理消息的具体逻辑
     * @param message
     */
    public void processScoringMessage(DeepSeekResponse message) {
        // 实现具体业务逻辑
        logger.info("Processing Scoring Message: {}", message);
        // 解析message
        try {
            reviewAnalyzer.analyzeScoringResponse(message);

        }catch (JsonProcessingException e){
            logger.error("[130]JsonProcessingException: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("[145]IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("[129]Exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    //==========================2===========================
}