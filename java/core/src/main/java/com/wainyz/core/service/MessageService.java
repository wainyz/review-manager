package com.wainyz.core.service;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.utils.RedisOps;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.ClassMessage;
import com.wainyz.core.pojo.domain.FriendsMessage;
import com.wainyz.core.pojo.domain.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private FriendsService friendsService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FriendsMessageService friendsMessageService;
    @Autowired
    private WebSocketMessageService webSocketMessageService;
    @Autowired
    private RedisOps redisOps;
    @Autowired
    private ClassMessageService classMessageService;

    public void sendFriendMessage(Long userId, Long friendId, Long friendsId , String content) throws Exception {
        String key = RedisOps.RedisKeyPrefix.Friends_Message.prefix +"message:"+ friendsId;
        String startTimeKey = key+":starttime" ;
        String endTimeKey = key+":endtime";
        String messageKey = key+":list";
        if (!redisOps.exists(key)) {
            redisOps.set(key,"1");
            redisOps.set(startTimeKey, String.valueOf(System.currentTimeMillis()));
            redisOps.set(endTimeKey, String.valueOf(System.currentTimeMillis()));
        }
        //组装content
        content = RedisOps.buildFriendsMessageContent(userId,friendId,0,content);
        redisOps.set(endTimeKey, String.valueOf(System.currentTimeMillis()));
        // 首先 先存放在redis中。具体的格式如下:
        redisOps.safeAppendToList(messageKey, List.of(content));
        Notice notice = new Notice();
        notice.setTypeByEnum(NoticeTypeEnum.FriendMessage);
        notice.setContent(content);
        notice.setTimestamp(new Date());
        notice.setUserid(friendId);
        webSocketMessageService.sendMessageToUser(notice);
        //检查endtime
        long endtime = Long.parseLong(redisOps.get(endTimeKey));
        if(System.currentTimeMillis() > endtime + 60*2*100){
            clearRedisDataToDataBase(friendsId, RedisOps.RedisKeyPrefix.Friends_Message);
        }
    }
    private void clearRedisDataToDataBase(Long keyId, RedisOps.RedisKeyPrefix prefix){
        String key = prefix.prefix +"message:"+keyId;
        String startTimeKey =key+":starttime" ;
        String endTimeKey = key+":endtime";
        String messageKey = key+":list";
        if (!redisOps.exists(key)) {return;}

        if(prefix.equals(RedisOps.RedisKeyPrefix.Friends_Message)){
            // 提交到数据库,并创建新的消息块
            List<String> messages = redisOps.getEntireList(messageKey);
            FriendsMessage friendsMessage = new FriendsMessage();
            friendsMessage.setId(IdUtil.getSnowflakeNextId());
            friendsMessage.setFriendId(keyId);
            friendsMessage.setEndtime(new Date());
            friendsMessage.setStarttime(new Date(Long.parseLong(redisOps.get(startTimeKey))));
            // 关键修正：确保生成合法 JSON
            try {
                String jsonContent = objectMapper.writeValueAsString(messages);
                friendsMessage.setContent(jsonContent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON序列化失败", e);
            }
            friendsMessageService.save(friendsMessage);
        }else if( prefix.equals(RedisOps.RedisKeyPrefix.Class_Message)){
            // 提交到数据库,并创建新的消息块
            List<String> messages = redisOps.getEntireList(messageKey);
            ClassMessage classMessage = new ClassMessage();
            classMessage.setId(String.valueOf(IdUtil.getSnowflakeNextId()));
            classMessage.setClassId(keyId);
            classMessage.setEndtime(new Date());
            classMessage.setStarttime(new Date(Long.parseLong(redisOps.get(startTimeKey))));
            // 关键修正：确保生成合法 JSON
            try {
                String jsonContent = objectMapper.writeValueAsString(messages);
                classMessage.setContent(jsonContent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON序列化失败", e);
            }
             classMessageService.save(classMessage);
        }
        redisOps.delete(key);
        redisOps.delete(startTimeKey);
        redisOps.delete(endTimeKey);
        redisOps.delete(messageKey);
    }
    public void sendClassMessage(Long userId, Long classId,String className, String content) throws Exception {
        String key = RedisOps.RedisKeyPrefix.Class_Message.prefix +"message:"+ classId;
        String startTimeKey = key+":starttime" ;
        String endTimeKey = key+":endtime";
        String messageKey = key+":list";

        if (!redisOps.exists(key)) {
            redisOps.set(key,"1");
            redisOps.set(startTimeKey, String.valueOf(System.currentTimeMillis()));
            redisOps.set(endTimeKey, String.valueOf(System.currentTimeMillis()));
        }else{
            long endtime = Long.parseLong(redisOps.get(endTimeKey));
            if(System.currentTimeMillis() > (endtime + 60*2*100)){
                clearRedisDataToDataBase(classId, RedisOps.RedisKeyPrefix.Class_Message);
            }
        }
        //组装content
        content = RedisOps.buildClassMessageContent(userId, content);
        redisOps.set(endTimeKey, String.valueOf(System.currentTimeMillis()));
        // 首先 先存放在redis中。具体的格式如下:
        redisOps.safeAppendToList(messageKey, List.of(content));
        Notice notice = new Notice();
        notice.setId( IdUtil.getSnowflakeNextId());
        notice.setTypeByEnum(NoticeTypeEnum. ClassMessage);
        notice.setContent(content);
        notice.setTimestamp(new Date());
        notice.setUserid(userId);
        webSocketMessageService.sendMessageToClass(notice, classId);
        //检查endtime
    }
    public List<ClassMessage> listClassMessage(Long classId){
        List<ClassMessage> list = classMessageService.lambdaQuery().eq(ClassMessage::getClassId, classId).list();
        String key = RedisOps.RedisKeyPrefix.Class_Message.prefix + "message:" + classId;
        String startTimeKey = key+":starttime" ;
        String messageKey = key+":list";
        if(redisOps.exists(key)){
            List<String> messages = redisOps.getEntireList(messageKey);
            ClassMessage classMessage = new ClassMessage();
            // 关键修正：确保生成合法 JSON
            try {
                String jsonContent = objectMapper.writeValueAsString(messages);
                classMessage.setContent(jsonContent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON序列化失败", e);
            }
            classMessage.setStarttime(new Date(Long.parseLong(redisOps.get(startTimeKey))));
            classMessage.setEndtime(new Date());
            classMessage.setClassId(classId);
            classMessage.setId(String.valueOf(IdUtil.getSnowflakeNextId()));
            list.add(classMessage);
        }
        return list;
    }
}
