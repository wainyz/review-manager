package com.wainyz.core.service;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;
import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.core.analyizer.QuestionFileAnalyzer;
import com.wainyz.core.analyizer.ReviewAnalyzer;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.manager.QuickStatusManager;
import com.wainyz.core.pojo.domain.DeepSeekResponse;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.pojo.domain.Paper;
import lombok.Synchronized;
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
import java.util.Date;
import java.util.Map;
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
    @Autowired
    private PaperService paperService;
    @Autowired
    private NoticeService noticeService;
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    // 用于存储最近五次的等待时间，默认值给的60s
    private static final int[] waitingArray = new int[]{60,60,60,60,60};

    private static int waitingArrayIndex = 0;
    public static int getAverageWaitingTime(){return (int) Arrays.stream(waitingArray).average().getAsDouble();}

    /**
     * 当收到deepseek返回的时候，调用这个方法更新平均等待时间。
     * 需要注意这里的id是Notice id，配合等待通知，更新。
     * @param noticeId
     */
    @Synchronized
    private void updateWaitingTime(Long noticeId){
        Notice byId = noticeService.getById(noticeId);
        waitingArray[waitingArrayIndex] = (int) ((System.currentTimeMillis()-byId.getTimestamp().getTime())/100);
        waitingArrayIndex = (waitingArrayIndex+1)%waitingArray.length;
    }
//-----------------------------1--------------------
    /**
     * 处理生成试题paper的响应消息
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
    @Autowired
    private ExamService examService;
    /**
     * WARNING： 需要python配合，使用exam作为id后缀区分两种返回，做不同的处理。
     */
    private final String id_endWith_string = "exam";
    private void processGenerateQuestionMessage(DeepSeekResponse message) {
        // 实现具体业务逻辑
        logger.info("【110】Processing message: {}", message);
        Notice newNotice = new Notice();
        newNotice.setTypeByEnum(NoticeTypeEnum.OVER_WAITING);
        try {
            //判断是exam 还是 question, 根据message.getId 是否endWith exam 字符串
            if(message.getId().endsWith(id_endWith_string)){
                message.setId(message.getId().replace(id_endWith_string,""));
                //更新平均等待时间
                updateWaitingTime(Long.valueOf(message.getId()));
                // 获取 fileId，实际上是noticeId作为paperId
                String paperId = message.getId();
                Paper paper = new Paper();
                paper.setId(Long.valueOf(paperId));
                paper.setUserId(Long.valueOf(message.getUserId()));
                paper.setContent(message.getResponse());
                Notice notice = noticeService.getById(message.getId());
                Map<String, String> parser = NoticeTypeEnum.WAITING_GENERATION.parser(notice.getContent());
                paper.setTitle(parser.get(NoticeTypeEnum.WAITING_GENERATION.otherInfo[0]));
                // 创建 完成通知，通知相关用户。
                if (!paperService.save(paper)) {
                    throw new IOException("保存失败。");
                }
                newNotice.setContent(NoticeTypeEnum.OVER_WAITING.stringify(String.valueOf(1),message.getId()));
            }else{
                // else
                reviewAnalyzer.analyzeGenerateQuestionResponse(message.getId(), message);
                newNotice.setContent(NoticeTypeEnum.OVER_WAITING.stringify(String.valueOf(0),message.getId()));
            }
            //  删除等待通知，新建完成同桌
            boolean b = noticeService.removeById(message.getId());
            if (b){
                newNotice.setTimestamp(new Date());
                newNotice.setUserid(Long.valueOf(message.getUserId()));
                newNotice.setId(IdUtil.getSnowflakeNextId());
                newNotice.setContent(NoticeTypeEnum.OVER_WAITING.stringify());
                noticeService.saveAndNoticeUser(newNotice);
            }else{
                throw new IOException("[149]无法删除旧通知。");
            }
        } catch (Exception e) {
            logger.error("处理Mq消息出现异常："+ e);
            throw new RuntimeException(e);
        }
    }
//============================1===========================
//-----------------------2  打分逻辑---------------------
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