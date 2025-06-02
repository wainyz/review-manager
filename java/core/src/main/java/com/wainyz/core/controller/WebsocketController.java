package com.wainyz.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.pojo.domain.LastRead;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.LastReadService;
import com.wainyz.core.service.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.Date;
import java.util.List;

/**
 * @author Yanion_gwgzh
 */
@Controller
public class WebsocketController {
    private Logger logger = LoggerFactory.getLogger(WebsocketController.class);
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private LastReadService lastReadService;
    @Autowired
    private ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    public WebsocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 获取所有未读通知
     */
    @PostMapping("/unread")
    public ReturnModel getUnreadNotices(@RequestAttribute(GatewayConsistent.USER_ID) Long userId ) throws JsonProcessingException {
        // 这里的userid=-1是全局信息的标志
        LastRead one = lastReadService.lambdaQuery().eq(LastRead::getId, userId).one();
        Date lastReadTime;
        if(one == null){
            lastReadTime = new Date();
        }else{
            lastReadTime = one.getTimestamp();
        }
        List<Notice> list = noticeService.lambdaQuery().eq(Notice::getUserid, -1).gt(Notice::getTimestamp, lastReadTime).or().eq(Notice::getUserid, userId).gt(Notice::getTimestamp, lastReadTime).list();
        return ReturnModel.success().setData(objectMapper.writeValueAsString(list));
    }

    /**
     * 测试 hello
     */
    @MessageMapping("/hello")
    @SendTo("/all/notice")
    public String testHello(String message){
        logger.info("[收到消息]:{}",message);
        // 发送给目标用户（假设用户已认证，目标用户ID为 message.getTargetUserId()）
        return "[私信] 来自 " + message;
    }
}
