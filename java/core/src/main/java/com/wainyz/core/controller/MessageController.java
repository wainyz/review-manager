package com.wainyz.core.controller;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.commons.utils.RedisOps;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.Friends;
import com.wainyz.core.pojo.domain.FriendsMessage;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.FriendsMessageService;
import com.wainyz.core.service.FriendsService;
import com.wainyz.core.service.WebSocketMessageService;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author Yanion_gwgzh
 */
@RequestMapping("message")
@RestController
public class MessageController {
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
    @PostMapping("/talk")
    @Synchronized
    public ReturnModel talk(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
     @RequestParam("friendId") Long friendId, @RequestParam("content") String content
    ) {
        // 首先检查是否有这个好友
        Long smallId = userId < friendId? userId: friendId;
        Long bigId = !userId.equals(smallId) ? userId: friendId;
        Friends one = null;
        try {
           one = friendsService.lambdaQuery().eq(Friends::getSmallid, smallId).eq(Friends::getBigid, bigId).list().get(0);
        }catch (Exception e){
            return ReturnModel.fail().setMessage("好友不存在.");
        }
        if(one == null){
            return ReturnModel.fail().setMessage("好友不存在.");
        }
//        FriendsMessage friendMessage = friendsMessageService.lambdaQuery().eq(FriendsMessage::getFriendId, one.getId())
//                .orderBy(true, false, FriendsMessage::getId).list().get(0);
        String key = RedisOps.RedisKeyPrefix.Friends_Message.prefix+"message:"+one.getId();
        String startTimeKey =key+":starttime" ;
        String endTimeKey = key+":endtime";
        String messageKey = key+":list";
        //检查endtime
        if (!redisOps.exists(key)) {
            redisOps.set(key,"1");
            redisOps.set(startTimeKey, String.valueOf(System.currentTimeMillis()));
            redisOps.set(endTimeKey, String.valueOf(System.currentTimeMillis()));
        }else{
            long endtime = Long.parseLong(redisOps.get(endTimeKey));
            if(System.currentTimeMillis() > (endtime + 60*2*100)){
                clearRedisDataToDataBase(one.getId(), RedisOps.RedisKeyPrefix.Friends_Message);
            }
        }
        //组装content
        content = RedisOps.buildFriendsMessageContent(userId,friendId,0,content);
        redisOps.set(endTimeKey, String.valueOf(System.currentTimeMillis()));
        // 首先 先存放在redis中。具体的格式如下:
        redisOps.safeAppendToList(messageKey,List.of(content));
        Notice notice = new Notice();
        notice.setTypeByEnum(NoticeTypeEnum.FriendMessage);
        notice.setContent(content);
        notice.setTimestamp(new Date());
        notice.setUserid(String.valueOf(friendId));
        webSocketMessageService.sendMessageToUser(notice);

        return ReturnModel.success();
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
        }
        redisOps.delete(key);
        redisOps.delete(startTimeKey);
        redisOps.delete(endTimeKey);
        redisOps.delete(messageKey);
    }

    @GetMapping("/history/{friendId}")
    public ReturnModel getHistoryMessages(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                          @PathVariable("friendId") Long friendId){
        Long bigId = userId > friendId?userId:friendId;
        Long smallId = bigId.equals(userId)?friendId:userId;
        List<Friends> friendMessages = friendsService.lambdaQuery().eq(Friends::getBigid, bigId).eq(Friends::getSmallid, smallId).list();
        if( friendMessages.isEmpty()) {
            return ReturnModel.success().setData(List.of());
        }
        Friends friends = friendMessages.get(0);
        if(friends == null){
            return ReturnModel.fail().setMessage("并非好友。");
        }
//        clearRedisDataToDataBase(friends.getId());
        List<FriendsMessage> list = friendsMessageService.lambdaQuery().eq(FriendsMessage::getFriendId, friends.getId()).list();
        String key = RedisOps.RedisKeyPrefix.Friends_Message.prefix +"message:"+friends.getId();
        String startTimeKey =key+":starttime" ;
        String messageKey = key+":list";
        if(redisOps.exists(key)){
            List<String> messages = redisOps.getEntireList(messageKey);

            FriendsMessage friendsMessage = new FriendsMessage();
            // 关键修正：确保生成合法 JSON
            try {
                String jsonContent = objectMapper.writeValueAsString(messages);
                friendsMessage.setContent(jsonContent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON序列化失败", e);
            }
            friendsMessage.setStarttime(new Date(Long.parseLong(redisOps.get(startTimeKey))));
            friendsMessage.setEndtime(new Date());
            friendsMessage.setFriendId(friends.getId());
            friendsMessage.setId(IdUtil.getSnowflakeNextId());
            list.add(friendsMessage);
        }

        return ReturnModel.success().setData(list);
    }
}
