package com.wainyz.core.service;

import com.wainyz.core.pojo.domain.Class;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Yanion_gwgzh
* @description 针对表【class】的数据库操作Service
* @createDate 2025-05-24 19:43:03
*/
public interface ClassService extends IService<Class> {
    void addClass(Long userId, Long classId);
    void updateClass(Class newClass);
    boolean mySave(Class entity);
    void applyAddClass(Long userId, Long classId, String description, String username, String className) throws Exception;
    void rebackApply(Long userId, Long noticeId, int reback) throws Exception;
    void kickOutUser(Long operationUserId, Long classId, Long outUserId) throws Exception;
    boolean isInClass(Long userId, Long classId);
    void subscriptExam(Long userId, Long classId, Long examId) throws Exception;
}
