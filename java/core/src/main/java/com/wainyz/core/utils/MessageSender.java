package com.wainyz.core.utils;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.commons.consistent.RedisKeyConsistent;
import com.wainyz.commons.utils.RedisOps;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.DeepSeekRequestDO;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.NoticeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * &#064;description:  将deepseek调用请求发送到rabbitMq中，等待python代码来实现具体的调用。
 * @author Yanion_gwgzh
 */
@Component
public class MessageSender {
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisOps redisOps;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NoticeService noticeService;

    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

/**
 * 发送deepseek调用细节到rabbitMq中
 *
 * @return
 */
    public Notice sendDeepSeekRequest(DeepSeekRequestDO requestDO) throws JsonProcessingException {
        // 1生成notice
        if(requestDO.getMessageId()==null ||requestDO.getMessageId().isEmpty()){
            requestDO.setMessageId(String.valueOf(IdUtil.getSnowflake().nextId()));
        }
        Notice waitingNotice = getNoticeFormDeepSeekRequestDO(requestDO);
        //  2保存并发送notice
        noticeService.saveAndNoticeUser(waitingNotice);
        // 3 发送rabbitMq消息
        rabbitTemplate.convertAndSend(RabbitMqConsistent.EXCHANGE_NAME, RabbitMqConsistent.ROUTING_KEY, requestDO, message -> {
            message.getMessageProperties().setMessageId(String.valueOf(requestDO.getMessageId()));
            return message;
        });
        // 4 增加redis中的数量记录
        if(requestDO.getDeepSeekRequestEnum()== DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_PAPER
        || requestDO.getDeepSeekRequestEnum()== DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_TEST){
            redisOps.safeIncrement(RedisKeyConsistent.question_request.name());
        }else{
            redisOps.safeIncrement(RedisKeyConsistent.scoring_request.name());
        }
        return waitingNotice;
    }

    private static Notice getNoticeFormDeepSeekRequestDO(DeepSeekRequestDO requestDO) {
        Notice waitingNotice = new Notice();
        waitingNotice.setId(requestDO.getMessageId());
        waitingNotice.setUserid(requestDO.getUserId());
        waitingNotice.setTimestamp(new Date());
        switch (requestDO.getDeepSeekRequestEnum()){
            case GENERATE_TEST -> {
                waitingNotice.setTypeByEnum(NoticeTypeEnum.WAITING_GENERATION);
                waitingNotice.setContent(requestDO.getDeepSeekRequestEnum().chineseName);
            }
            case SCORING_ANSWERS -> {
                waitingNotice.setTypeByEnum(NoticeTypeEnum.WAITING_SCORING);
                waitingNotice.setContent(requestDO.getDeepSeekRequestEnum().chineseName);
            }
            case GENERATE_PAPER -> {
                // 设置title
                waitingNotice.setTypeByEnum(NoticeTypeEnum.WAITING_GENERATION);
                waitingNotice.setContent(NoticeTypeEnum.WAITING_GENERATION.stringify(requestDO.getParams()));
            }
            default -> throw new RuntimeException("未知的请求类型");
        }
        return waitingNotice;
    }
}
