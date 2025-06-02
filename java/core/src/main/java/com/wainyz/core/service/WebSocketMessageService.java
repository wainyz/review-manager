package com.wainyz.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wainyz.core.config.WebSocketConfig;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Yanion_gwgzh
 */
@Service
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketMessageService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    /**
     * 根据通知类型通知 应该通知的用户
     */
    public void noticeSomeUser(Notice notice) throws JsonProcessingException {
        //判断通知类型
        if(notice.getType().equals(NoticeTypeEnum.SYSTEM_ALL.value)){
            noticeAllUsers(notice);
        } else {
            noticeToUser(notice.getUserid(), notice);
        }
    }

    /**
     * 发送全局通知
     */
    public void noticeAllUsers(Object content) {
        messagingTemplate.convertAndSend(WebSocketConfig.ALL_NOTICE, content);
    }
    /**
     * 发送指定通知到用户
     */
    public void noticeToUser(Long userId, Object content) {
        messagingTemplate.convertAndSend( "/user/"+userId+"/notice", content);
    }
    /**
     * 发送好友消息
     */
    public void sendMessageToUser(Long userId, Object content){
        messagingTemplate.convertAndSend("/user/"+userId+"/message", content);
    }
    public void sendMessageToUser(Notice notice){
        messagingTemplate.convertAndSend("/user/"+notice.getUserid()+"/message", notice);
    }
    public void  sendMessageToClass(Notice notice,  Long classId){
        messagingTemplate.convertAndSend("/class/"+classId+"/message", notice);
    }
}