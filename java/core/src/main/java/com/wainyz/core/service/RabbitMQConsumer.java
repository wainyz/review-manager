package com.wainyz.core.service;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;
import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.commons.consistent.RedisKeyConsistent;
import com.wainyz.commons.utils.RedisOps;
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
import java.util.concurrent.TimeUnit;

import static com.wainyz.core.pojo.domain.DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_PAPER;
import static com.wainyz.core.pojo.domain.DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_TEST;

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
    @Autowired
    private RedisOps redisOps;
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
            // 4 减少redis
            redisOps.safeDecrement(RedisKeyConsistent.question_request.name());
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
        logger.info("【110】处理生成测试题的大模型响应: {}", message);
        // 1 更新平均等待时间
        updateWaitingTime(Long.valueOf(message.getId()));
        // 2 组装 等待完成 通知
        Notice overNotice = new Notice();
        overNotice.setId(String.valueOf(IdUtil.getSnowflakeNextId()));
        overNotice.setUserid(message.getUserId());
        overNotice.setTimestamp(new Date());
        overNotice.setTypeByEnum(NoticeTypeEnum.OVER_WAITING);
        try {
            //判断是exam 还是 test,
            if(message.getDeepSeekRequestEnum().equals(GENERATE_PAPER)){
                // 3 保存paper
                Paper paper = new Paper();
                paper.setId(message.getId());
                paper.setUserId(message.getUserId());
                paper.setContent(message.getResponse());
                paper.setTitle(message.getParams());
                // Warning: 可能的错误：paper保存完毕即通知相关用户。
                if (!paperService.save(paper)) {
                    throw new IOException("保存失败。");
                }
                // 2.1 完成组装 等待完成 通知。
                overNotice.setContent(NoticeTypeEnum.OVER_WAITING.stringify(message.getParams(),"1",message.getId()));
            }else if(message.getDeepSeekRequestEnum().equals(GENERATE_TEST)){
                // 3 分析并保存test
                reviewAnalyzer.analyzeGenerateQuestionResponse(message.getParams(), message);
                //2.1 完成组装 等待完成 通知。
                overNotice.setContent(NoticeTypeEnum.OVER_WAITING.stringify("测试题",String.valueOf(0),message.getId()));
            }
            // 4 删除 等待通知
            boolean b = noticeService.removeById(message.getId());
            if (b){
                // 5 保存并发送 完成等待通知
                noticeService.saveAndNoticeUser(overNotice);
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
            // 4 减少redis
            redisOps.safeDecrement(RedisKeyConsistent.scoring_request.name());
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
        try {
            // 1 处理逻辑
            reviewAnalyzer.analyzeScoringResponse(message);
            // 2 移除旧地等待通知
            noticeService.removeById(message.getId());
            // 2 构建、保存、发送 完成等待通知
            Notice build = Notice.build(message.getUserId(), NoticeTypeEnum.OVER_WAITING, "测试题", String.valueOf(3), message.getId());
            noticeService.saveAndNoticeUser(build);
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