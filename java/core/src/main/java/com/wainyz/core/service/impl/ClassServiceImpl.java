package com.wainyz.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.mapper.ClassMapper;
import com.wainyz.core.pojo.domain.Class;
import com.wainyz.core.pojo.domain.MyClassList;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.ClassService;
import com.wainyz.core.service.MyClassListService;
import com.wainyz.core.service.NoticeService;
import com.wainyz.core.service.WebSocketMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yanion_gwgzh
 * &#064;description  针对表【class】的数据库操作Service实现
 * &#064;createDate  2025-05-24 19:43:03
 */
@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class>
        implements ClassService {
    Logger logger = LoggerFactory.getLogger(ClassServiceImpl.class);
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private WebSocketMessageService webSocketMessageService;
    @Autowired
    private MyClassListService myClassListService;

    @Override
    public void addClass(Long userId, Long classId) {
        // 首先加入class，然后加入myClassList
        Class thisClass = getById(classId);
        if(thisClass == null){
            return;
        }
        List<Long> studentList = Class.convertToList(thisClass.getStudentList());
        studentList.add(userId);
        try {
            boolean update = lambdaUpdate().set(Class::getStudentList, objectMapper.writeValueAsString(studentList)).eq(Class::getId, classId).update();
            if (!update) {
                logger.error("[38]更新失败");
            }
        } catch (Exception e) {
            logger.error("[41]转换失败");
        }
        //  加入myClassList
        MyClassList myclassList = myClassListService.getById(userId);
        if (myclassList == null) {
            MyClassList myClassList = new MyClassList();
            myClassList.setId(userId);
            List<Long> classesByList = new ArrayList<>();
            classesByList.add(classId);
            myClassList.setClassesByList(classesByList);
            myClassListService.save(myClassList);
        } else {
            try {
                List<Long> classesByList = myclassList.getClassesByList();
                classesByList.add(classId);
                myclassList.setClassesByList(classesByList);
                boolean b = myClassListService.updateById(myclassList);
                if (!b) {
                    logger.error("[51]更新失败");
                }
            } catch (Exception e) {
                logger.error("[64]转换失败");
            }
        }
    }

    /**
     * 实际操作是这样的，首先更新信息会发送给之前的所有student和teacher，然后更新class，然后更新myClassList
     */

    @Override
    public void updateClass(Class newClass) {
        // 更新class
        Class oldInfo = getById(newClass.getId());
        lambdaUpdate().set(Class::getClassName, newClass.getClassName())
                .set(Class::getDescription, newClass.getDescription())
                .set(Class::getIsPublic, newClass.getIsPublic())
                .set(Class::getStudentList, newClass.getStudentList())
                .set(Class::getTeacherList, newClass.getTeacherList())
                .eq(Class::getId, newClass.getId())
                .update();
        try {
            // 发送通知
            Notice notice = new Notice();
            if (objectMapper.readTree(oldInfo.getStudentList()) != null) {
                for (Long studentId : objectMapper.readValue(oldInfo.getStudentList(), Long[].class)) {
                    notice.setUserid(studentId);
                    notice.setTypeByEnum(NoticeTypeEnum.CLASS_UPDATE);
                    notice.setTimestamp(new Date());
                    notice.setId(IdUtil.getSnowflake().nextId());
                    String updateInfo = "";
                    //如果用户不在列表中，则被kick out
                    if (!newClass.getStudentList().contains(studentId.toString())) {
                        updateInfo = "您已被踢出班级";
                    } else {
                        updateInfo = "班级 " + oldInfo.getClassName() + " 信息更新";
                        if (!oldInfo.getClassName().equals(newClass.getClassName())) {
                            updateInfo += ",名称更新为：" + newClass.getClassName();
                        }
                        if (!oldInfo.getDescription().equals(newClass.getDescription())) {
                            updateInfo += ",描述更新为：" + newClass.getDescription();
                        }
                        if (oldInfo.getIsPublic() != newClass.getIsPublic()) {
                            updateInfo += ",公开状态更新为：" + newClass.getIsPublic();
                        }
                    }
                    //否则推送更新消息
                    notice.setContent(NoticeTypeEnum.CLASS_UPDATE.stringify(String.valueOf(newClass.getId()), updateInfo));
                    noticeService.saveAndNoticeUser(notice);
                }
            } else {
                logger.error("[100]班级元数据存储的json格式有问题。");
            }
        } catch (JsonProcessingException e) {
            logger.error("[101]转换失败");
        }
    }

    @Override
    @Transactional
    public boolean mySave(Class entity) {
        save(entity);
        MyClassList classList = myClassListService.getById(entity.getOwner());
        List<Long> classesByList = classList.getClassesByList();
        classesByList.add(Long.valueOf(entity.getId()));
        myClassListService.updateById(classList.setClassesByList(classesByList));
        return true;
    }

    @Override
    public void applyAddClass(Long userId, Long classId, String description) throws Exception {
        Class byId = getById(classId);
        if (byId != null) {
            Notice notice = new Notice();
            notice.setTypeByEnum(NoticeTypeEnum.ADD_CLASS_APPLY);
            notice.setUserid(byId.getOwner());
            notice.setTimestamp(new Date());
            notice.setId(IdUtil.getSnowflake().nextId());
            notice.setContent(NoticeTypeEnum.ADD_CLASS_APPLY.stringify(String.valueOf(userId), String.valueOf(classId), description));
            noticeService.saveAndNoticeUser(notice);
        } else {
            throw new Exception("[104]班级不存在");
        }
    }

    @Override
    public void rebackApply(Long userId, Long noticeId, int reback) throws Exception {
        // 首先先去查询是否有这个用户的请求
        Notice byId = noticeService.getById(noticeId);
        String applyUserId = byId.getContent().split(",")[0].substring(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[0].length() + 1);
        String applyGoalClassId = byId.getContent().split(",")[1].substring(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[1].length() + 1);

        boolean permission = lambdaQuery().eq(Class::getId, applyGoalClassId).eq(Class::getOwner, userId).count() != 0;
        if (!permission) {
            throw new Exception("[158]权限不足");
        }
        if (reback == 0) {
            //表示拒绝
            Notice build = Notice.build(Long.valueOf(applyUserId), NoticeTypeEnum.REJECT_CLASS_APPLY, applyGoalClassId);
            noticeService.saveAndNoticeUser(build);
        } else {
            Class classObject = getById(applyGoalClassId);
            List<Long> longs = Class.convertToList(classObject.getStudentList());
            longs.add(Long.valueOf(applyUserId));
            lambdaUpdate().set(Class::getStudentList, objectMapper.writeValueAsString(longs)).update();
            MyClassList byId1 = myClassListService.getById(applyUserId);
            List<Long> classesByList = byId1.getClassesByList();
            classesByList.add(Long.valueOf(applyGoalClassId));
            myClassListService.updateById(new MyClassList().setId(Long.valueOf(applyUserId)).setClassesByList(classesByList));
            noticeService.saveAndNoticeUser(Notice.build(Long.valueOf(applyUserId), NoticeTypeEnum.AGREE_CLASS_APPLY, applyGoalClassId));
        }

    }

    @Override
    public void kickOutUser(Long operationUserId, Long classId, Long outUserId) throws Exception {
        Class aClass = getById(classId);
        if (aClass == null) {
            throw new Exception("班级不存在");
        }
        boolean permission = aClass.getOwner().equals(operationUserId) || operationUserId.equals(outUserId);
        if (!permission) {
            throw new Exception("权限不足");
        }
        if (aClass.getOwner().equals(outUserId)) {
            throw new Exception("不能将班级的创建者移出班级");
        }
        if (!aClass.getStudentList().contains(outUserId.toString())) {
            if (!aClass.getTeacherList().contains(outUserId.toString())) {
                throw new Exception("[155]用户不在班级中");
            }else{
                List<Long> longs = Class.convertToList(aClass.getTeacherList());
                longs.remove(outUserId);
                lambdaUpdate().set(Class::getTeacherList, longs).update();
            }
        }else{
            List<Long> longs = Class.convertToList(aClass.getStudentList());
            longs.remove(outUserId);
            lambdaUpdate().set(Class::getStudentList, objectMapper.writeValueAsString(longs)).update();
        }
    }

    @Override
    public boolean isInClass(Long userId, Long classId) {
        List<Class> list = lambdaQuery().eq(Class::getId, classId).list();
        if (list.isEmpty()){
            return false;
        }
        Class aClass = (Class) list.get(0);
        return aClass.getOwner().equals(userId) || !aClass.getTeacherList().contains(userId.toString())
        || !aClass.getStudentList().contains(userId.toString());
    }

    @Override
    public void subscriptExam(Long userId, Long classId, Long examId) throws Exception {

    }
}




