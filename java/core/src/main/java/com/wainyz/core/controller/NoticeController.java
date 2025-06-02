package com.wainyz.core.controller;

import cn.hutool.core.util.IdUtil;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.LastRead;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.LastReadService;
import com.wainyz.core.service.NoticeService;
import com.wainyz.core.service.RabbitMQConsumer;
import com.wainyz.user.controller.PermissionApiController;
import com.wainyz.user.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    NoticeService noticeService;
    @Autowired
    UserPermissionService userPermissionService;

    @GetMapping("/list")
    public ReturnModel getAllNotice(@RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        List<Notice> userAllNotice = noticeService.getUserAllNotice(userId);
        return ReturnModel.success().setData(userAllNotice);
    }
    @GetMapping("/get/{noticeId}")
    public ReturnModel getNotice(@PathVariable("noticeId") Long noticeId, @RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        Notice one = noticeService.lambdaQuery().eq(Notice::getUserid, userId).eq(Notice::getId, noticeId).one();
        return ReturnModel.success().setData(one);
    }
    @Autowired
    private LastReadService lastReadService;
    @GetMapping("/unread/list")
    public ReturnModel listUnreadNotice(@RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        // 这里的userid=-1是全局信息的标志
        LastRead one = lastReadService.lambdaQuery().eq(LastRead::getId, userId).one();
        Date lastReadTime;
        if(one == null){
            lastReadTime = new Date();
        }else{
            lastReadTime = one.getTimestamp();
        }
        List<Notice> list = noticeService.lambdaQuery().eq(Notice::getUserid, -1).gt(Notice::getTimestamp, lastReadTime).or().eq(Notice::getUserid, userId).gt(Notice::getTimestamp, lastReadTime).list();
        return ReturnModel.success().setData(list);
    }
    @GetMapping("/get/last_read_time")
    public ReturnModel getLastReadTime(@RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        LastRead byId = lastReadService.getById(userId);
        if(byId == null){
            LastRead lastRead = new LastRead();
            lastRead.setId(userId);
            lastRead.setTimestamp(new Date());
            lastReadService.save(lastRead);
            return ReturnModel.success().setData(lastRead.getTimestamp());
        }
        return ReturnModel.success().setData(byId.getTimestamp());
    }
    @PostMapping("/update/last_read_time")
    public ReturnModel updateLastReadTime(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
     @RequestParam Long timestamp){
        // 这里的userid=-1是全局信息的标志
        if (lastReadService.lambdaUpdate().eq(LastRead::getId, userId).set(LastRead::getTimestamp, new Date(timestamp)).update()) {
            return ReturnModel.success().setMessage("更新成功.");
        }else {
            LastRead lastRead = new LastRead();
            lastRead.setId(userId);
            lastRead.setTimestamp(new Date());
            if (lastReadService.save(lastRead)) {
                return ReturnModel.success().setMessage("更新成功。");
            }else{
                return ReturnModel.fail().setMessage("更新失败.");
            }
        }
    }

    @PostMapping("/all")
    public ReturnModel noticeAllUser(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestBody String noticeContent){
        if (!userPermissionService.checkUserPermission(userId, PermissionApiController.PermissionType.notice)) {
            return ReturnModel.fail().setMessage("权限不足");
        }
        Notice notice = new Notice();
        notice.setId(IdUtil.getSnowflake().nextId());
        notice.setTimestamp(new Date());
        notice.setTypeByEnum(NoticeTypeEnum.SYSTEM_ALL);
        notice.setContent(noticeContent);
        notice.setUserid(-1L);
        if (noticeService.saveAndNoticeUser(notice)) {
            return ReturnModel.success().setMessage("通知成功.");
        }else{
            return ReturnModel.fail().setMessage("失败。");
        }
    }
    @PostMapping("/user/{userId}")
    public ReturnModel noticeUser(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,
            @PathVariable("userId") Long noticeGoal,
                                  @RequestBody String content){
        if (!userPermissionService.checkUserPermission(userId, PermissionApiController.PermissionType.notice)) {
            return ReturnModel.fail().setMessage("权限不足");
        }
        Notice notice = new Notice();
        notice.setId(IdUtil.getSnowflake().nextId());
        notice.setTimestamp(new Date());
        notice.setTypeByEnum(NoticeTypeEnum.SYSTEM_ONE);
        notice.setContent(content);
        notice.setUserid(noticeGoal);
        if (noticeService.saveAndNoticeUser(notice)) {
            return ReturnModel.success().setMessage("通知成功.");
        }else{
            return ReturnModel.fail().setMessage("失败。");
        }
    }
    @GetMapping("/waiting/average_time")
    public ReturnModel getWaitingAverageTime(@RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        return ReturnModel.success().setData(RabbitMQConsumer.getAverageWaitingTime());
    }

}     
