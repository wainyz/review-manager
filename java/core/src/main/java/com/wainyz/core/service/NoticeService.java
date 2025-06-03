package com.wainyz.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wainyz.core.pojo.domain.Notice;

import java.util.List;

/**
* @author Yanion_gwgzh
* @description 针对表【notice】的数据库操作Service
* @createDate 2025-05-13 13:36:33
*/
public interface NoticeService extends IService<Notice> {
    List<Notice> getUserAllNotice(Long userId);

    boolean addFriendApplyNotice(Long userId, String username, Long friendId);

    boolean agreeFriendApplyNotice(Long userId, String username, Long friendId);

    boolean saveAndNoticeUser(Notice notice);

    void rejectFriendApplyNotice(Long userId, String username, Long friendId);

    void sendNoticeToAllUser(Notice notice);
}
