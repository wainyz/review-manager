package com.wainyz.core.utils;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.RabbitMqConsistent;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.manager.QuickStatusManager;
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
    private QuickStatusManager quickStatusManager;
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
        // 设置messageId,作为去重的标识
        Long noticeId =  IdUtil.getSnowflakeNextId();
        requestDO.setFileId(noticeId.toString());
        String messageId = requestDO.getUserId()+":"+noticeId;
        rabbitTemplate.convertAndSend(RabbitMqConsistent.EXCHANGE_NAME, RabbitMqConsistent.ROUTING_KEY, requestDO, message -> {
            message.getMessageProperties().setMessageId(messageId);
            return message;
        });
        //生成等待通知
        Notice notice = new Notice();
        notice.setId(noticeId);
        if( requestDO.getDeepSeekRequestEnum() == DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_PAPER){
            JsonNode jsonNode = objectMapper.readTree(requestDO.getUserContent());
            notice.setTypeByEnum(NoticeTypeEnum.WAITING_GENERATION);
            notice.setContent(NoticeTypeEnum.WAITING_GENERATION.stringify(jsonNode.get("name").asText()));
        }else if(requestDO.getDeepSeekRequestEnum() == DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_TEST){
            notice.setType(NoticeTypeEnum.WAITING_GENERATION.value);
            notice.setContent(requestDO.getDeepSeekRequestEnum().getChineseName());
        }else{
            notice.setType(NoticeTypeEnum.WAITING_SCORING.value);
            notice.setContent(requestDO.getDeepSeekRequestEnum().getChineseName());
        }
        notice.setTimestamp(new Date());
        notice.setUserid(Long.valueOf(requestDO.getUserId()));
        noticeService.saveAndNoticeUser(notice);
//        if (quickStatusManager.shouldBeginRecordSend()){
//            quickStatusManager.sendRecord(messageId);
//        }
        return notice;
    }
}
