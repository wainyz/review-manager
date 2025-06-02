package com.wainyz.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.mapper.NoticeMapper;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.NoticeService;
import com.wainyz.core.service.WebSocketMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
* @author Yanion_gwgzh
* &#064;description  针对表【notice】的数据库操作Service实现
* &#064;createDate  2025-05-13 13:36:33
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService{
    @Autowired
    private WebSocketMessageService webSocketMessageService;

    @Override
    public List<Notice> getUserAllNotice(Long userId) {
        return lambdaQuery().eq(Notice::getUserid, userId).or().eq(Notice::getType, NoticeTypeEnum.SYSTEM_ALL.value).list();
    }

    /**
     * 申请加好友通知
     * @param userId
     * @param friendId
     * @return
     */
    @Override
    public boolean addFriendApplyNotice(Long userId, String username, Long friendId) {
        // 发送通知
        Notice notice = new Notice();
        notice.setId(String.valueOf(IdUtil.getSnowflake().nextId()));
        notice.setTypeByEnum(NoticeTypeEnum.ADD_FRIEND_APPLY);
        notice.setContent(NoticeTypeEnum.ADD_FRIEND_APPLY.stringify(userId.toString(),username ));
        notice.setUserid(String.valueOf(friendId));
        notice.setTimestamp(new Date());
        // 保存和通知
        return saveAndNoticeUser(notice);
    }

    /**
     * 同意好友请求，并返回好友申请通过notice
     * @param userId
     * @param friendId
     * @return
     */
    @Override
    public boolean agreeFriendApplyNotice(Long userId,String username, Long friendId) {
        //检查 是否有申请通知
        Notice one = null;
        try {
            one = lambdaQuery()
                    .eq(Notice::getType, NoticeTypeEnum.ADD_FRIEND_APPLY.value)
                    .eq(Notice::getUserid, userId)
                    .like(Notice::getContent, friendId + ",%").list().get(0);
        }catch (Exception ignored){}
        if( one == null){
            return false;
        }
        //删除 通知
        removeById(one);
        //新建 通过申请的通知
        Notice notice = new Notice();
        notice.setId(String.valueOf(IdUtil.getSnowflake().nextId()));
        notice.setTimestamp(new Date());
        notice.setTypeByEnum(NoticeTypeEnum.AGREE_FRIEND_APPLY);
        notice.setUserid(String.valueOf(friendId));
        notice.setContent(NoticeTypeEnum.AGREE_FRIEND_APPLY.stringify(friendId.toString(), username));
        return saveAndNoticeUser(notice);
        // 保存好友到数据库的操作放在friendService中
    }

    @Override
    public boolean saveAndNoticeUser(Notice notice) {
        if (save(notice)) {
            // 通知在线用户
            webSocketMessageService.sendMessageToUser(notice);
            return true;
        }else{
            return false;
        }
    }


}




