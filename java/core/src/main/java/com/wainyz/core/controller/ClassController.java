package com.wainyz.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.Class;
import com.wainyz.core.pojo.domain.MyClassList;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.ClassService;
import com.wainyz.core.service.MessageService;
import com.wainyz.core.service.MyClassListService;
import com.wainyz.core.service.NoticeService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("class")
public class ClassController {
    @Autowired
    private ClassService classService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private MyClassListService myClassListService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/create")
    public ReturnModel createClass(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("className") String className, @RequestParam("description") String description,
                                   @RequestParam("isPublic") int isPublic,
                                   @RequestParam("param") @Nullable String param
    ) throws JsonProcessingException {
        Class aClass = new Class();
        aClass.setClassName(className);
        aClass.setOwner(userId);
        aClass.setDescription(description);
        aClass.setIsPublic(isPublic);
        String empty = objectMapper.writeValueAsString(List.of());
        aClass.setExamList(empty);
        aClass.setTeacherList(empty);
        aClass.setStudentList(empty);
        classService.mySave(aClass);
        return ReturnModel.success().setData(aClass.getId());
    }
    @PostMapping("/update")
    public ReturnModel updateClass(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("classId") Long classId,
                                   @RequestParam("className") String className,
                                   @RequestParam("description") String description,
                                   @RequestParam("isPublic") int isPublic,
                                   @RequestParam("studentList") String studentList,
                                   @RequestParam("teacherList") String teacherList,
                                   @RequestParam("param")@Nullable String param
    ) throws JsonProcessingException {
        Class aClass = new Class();
        aClass.setId(String.valueOf(classId));
        aClass.setClassName(className);
        aClass.setOwner(userId);
        aClass.setDescription(description);
        aClass.setIsPublic(isPublic);
        // 这里排序是为了在后续发送通知的时候判断用户是否被删除。
        if(!studentList.trim().isEmpty()){
            List<Long> students = Arrays.stream(studentList.split(",")).map(Long::valueOf).toList();
            aClass.setStudentList(objectMapper.writeValueAsString(students));
        }
        if (!teacherList.trim().isEmpty()){
            List<Long> teachers = Arrays.stream(teacherList.split(",")).map(Long::valueOf).toList();
            aClass.setTeacherList(objectMapper.writeValueAsString(teachers));
        }
        // 更新班级信息，并发送通知
        classService.updateClass(aClass);
        return ReturnModel.success().setData(aClass.getId());
    }
    @PostMapping("/apply/add")
    public ReturnModel applyAddClass(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("classId") Long classId,
                                     @RequestParam("description") String description){
        try {
            classService.applyAddClass(userId, classId, description);
            return ReturnModel.success();
        }catch (Exception e){
            return ReturnModel.fail().setMessage(e.getMessage());
        }
    }
    @PostMapping("/apply/reback")
    public ReturnModel rebackApply(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                     @RequestParam("noticeId") Long noticeId,
                                   @RequestParam("reback")int reback){
        try {
            classService.rebackApply(userId, noticeId, reback);
            return ReturnModel.success();
        }catch (Exception e){
            e.printStackTrace();
            return ReturnModel.fail().setMessage(e.getMessage());
        }
    }
    @PostMapping("/kick_out")
    public ReturnModel kickOut(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("userId") Long outUserId,
                                   @RequestParam("classId") Long classId
                                   ){
        try {
            classService.kickOutUser(userId, classId,outUserId);
            return ReturnModel.success();
        }catch (Exception e){
            e.printStackTrace();
            return ReturnModel.fail().setMessage(e.getMessage());
        }
    }
    @PostMapping("/message")
    public ReturnModel classMessage(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                    @RequestAttribute(GatewayConsistent.USER_NAME) String username,
                               @RequestParam("classId") Long classId,
                               @RequestParam("content") String content
    ){
        try {
            Class byId = classService.getById(classId);
            if (byId == null){
                return ReturnModel.fail().setMessage("班级不存在");
            }else if (!classService.isInClass(userId, classId)){
                return ReturnModel.fail().setMessage("您不在该班级中");
            }
            messageService.sendClassMessage(userId, classId, username, content);
            return ReturnModel.success();
        }catch (Exception e){
            return ReturnModel.fail().setMessage(e.getMessage());
        }
    }
    //DOING: 待完成
    @PostMapping("/subscript")
    public ReturnModel subscriptExam(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                    @RequestParam("classId") Long outUserId,
                                    @RequestParam("examId") Long classId
    ){
        try {
            classService.kickOutUser(userId, classId,outUserId);
            return ReturnModel.success();
        }catch (Exception e){
            return ReturnModel.fail().setMessage(e.getMessage());
        }
    }
    @GetMapping("/my_classes")
    public ReturnModel listUserClass(@RequestAttribute(GatewayConsistent.USER_ID) Long userId
    ) throws JsonProcessingException {
        MyClassList classList = myClassListService.getById(userId);
        if(classList == null){
            return ReturnModel.success().setData(null);
        }
        ArrayList<Class> result = new ArrayList<>();
        for (Long classId : classList.getClassesByList()) {
            Class aclass = classService.getById(classId);
            //result.add(aclass.getId()+":"+aclass.getClassName());
            result.add(aclass);
        }
        return ReturnModel.success().setData(result);
    }

    @PostMapping("/search")
    public ReturnModel searchClass(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("key") String key){
        key = "%"+key+"%";
        List<Class> list = classService.query().eq("is_public", true).like("class_name", key).list();
        return ReturnModel.success().setData(list);
    }
    @PostMapping("/message/list")
    public ReturnModel messageList(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("classId") Long classId){
        if (!classService.isInClass(userId, classId)){
            return ReturnModel.fail().setMessage("您不在该班级中");
        }
        return ReturnModel.success().setData(messageService.listClassMessage(classId));
    }
    @PostMapping("/info")
    public ReturnModel info(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                   @RequestParam("classId") Long classId) {
        if (!classService.isInClass(userId, classId)) {
            return ReturnModel.fail().setMessage("您不在该班级中");
        }
        return ReturnModel.success().setData(classService.getById(classId));
    }
//    @PostMapping("/apply/reback")
//    public ReturnModel reback123(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
//     @RequestParam("classId") Long classId,
//     @RequestParam("description") String description
//     ){
//        Long owner = classService.getById(classId).getOwner();
//        Notice notice = new Notice();
//        notice.setUserid(owner);
//        notice.setTimestamp(new Date());
//        notice.setTypeByEnum(NoticeTypeEnum.ADD_CLASS_APPLY);
//        notice.setContent(NoticeTypeEnum.ADD_CLASS_APPLY.stringify(userId.toString(), classId.toString(),description));
//        notice.setId(IdUtil.getSnowflake().nextId());
//        noticeService.saveAndNoticeUser(notice);
//        return ReturnModel.success().setData(notice);
//    }

     @PostMapping("/reject/apply")
    public ReturnModel rejectApply(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
     @RequestParam("noticeId") Long noticeId
     ){

         Notice applyNotice = noticeService.getById(noticeId);
         Map<String, String> params = NoticeTypeEnum.ADD_CLASS_APPLY.parser(applyNotice.getContent());
         Notice notice = new Notice();
         if(!classService.getById(Long.valueOf(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[1]))).getOwner().equals(userId)){
             return ReturnModel.fail().setMessage("权限不足");
         }
         notice.setTypeByEnum(NoticeTypeEnum.REJECT_CLASS_APPLY);
         notice.setUserid(Long.valueOf(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[0])));
         notice.setTimestamp(new Date());
         notice.setContent(NoticeTypeEnum.REJECT_CLASS_APPLY.stringify(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[1])));
         noticeService.removeById(noticeId);
         noticeService.saveAndNoticeUser(notice);
         return ReturnModel.success();
     }
    @PostMapping("/agree/apply")
    public ReturnModel agreeApply(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
                                  @RequestParam("noticeId") Long noticeId
    ){
        Notice applyNotice = noticeService.getById(noticeId);
        Map<String, String> params = NoticeTypeEnum.ADD_CLASS_APPLY.parser(applyNotice.getContent());
        Notice notice = new Notice();
        notice.setTypeByEnum(NoticeTypeEnum.AGREE_CLASS_APPLY);
        notice.setUserid(Long.valueOf(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[0])));
        notice.setTimestamp(new Date());
        notice.setContent(NoticeTypeEnum.AGREE_CLASS_APPLY.stringify(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[0]),params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[1])));
        noticeService.removeById(noticeId);
        noticeService.saveAndNoticeUser(notice);
        // 加入班级
            // 先检查权限
        if(!classService.getById(Long.valueOf(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[1]))).getOwner().equals(userId)){
            return ReturnModel.fail().setMessage("权限不足");
        }
        classService.addClass(notice.getUserid(), Long.valueOf(params.get(NoticeTypeEnum.ADD_CLASS_APPLY.otherInfo[1])));
        return ReturnModel.success();
    }
     @PostMapping("/permission/kick_out")
    public ReturnModel kickOut1(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
      @RequestParam("classId") Long classId, @RequestParam("studentId") Long studentId
     ){
        //TODO: 待完善
        return ReturnModel.success();
     }
}
