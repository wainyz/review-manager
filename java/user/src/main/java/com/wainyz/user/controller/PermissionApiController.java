package com.wainyz.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.consistent.RedisKeyConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.commons.utils.RedisOps;
import com.wainyz.user.pojo.po.Ban;
import com.wainyz.user.pojo.po.PermissionRegistry;
import com.wainyz.user.service.BanService;
import com.wainyz.user.service.PermissionRegistryService;
import com.wainyz.user.service.UserPermissionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * 专门处理权限操作
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("/permission")
public class PermissionApiController {

    public enum PermissionType{
        showInfo(0),Update(1),notice(2);
         PermissionType(int permission){
            if(permission < 1 || permission > 31){

            }
            this.permission = permission;
        }
        public final int permission;
    }
    @Autowired
    private PermissionRegistryService permissionRegistryService;
    @Autowired
    private UserPermissionService userPermissionService;
    @Autowired
    private BanService banService;
    @Autowired
    private RedisOps redisOps;
    @Autowired
    private ObjectMapper objectMapper;
    @PostConstruct
    public void init(){
        // 注册权限
        for (PermissionType type : PermissionType.values()) {
            permissionRegistryService.registryServicePermission(type.name(),type.permission);
        }
    }
    private final String showRabbitMqDataTemplate = """
            {
              "question_request":${1},
              "scoring_request":${2}
            }
            """;
    //查看当前的rabbitMq的容量 1
    @PostMapping("/showRabbitMqInfo")
    public ReturnModel showRabbitMqData(@RequestAttribute(GatewayConsistent.USER_ID) Long userId) {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.showInfo.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        //实际上core模块维护在redis中的数据。这里的空字符串是为了防止redis中没有对应的键导致的null值调用异常。
        String data1 = showRabbitMqDataTemplate.replace("${1}", ""+redisOps.get(RedisKeyConsistent.question_request.name()));
        String data = data1.replace("${2}", ""+redisOps.get(RedisKeyConsistent.scoring_request.name()));
        return ReturnModel.success().setData(data);
    }
    @PostMapping("/showWebsocket")
    public ReturnModel showWebsocket(@RequestAttribute(GatewayConsistent.USER_ID) Long userId) {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.showInfo.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        //实际上core模块维护在redis中的数据。这里的空字符串是为了防止redis中没有对应的键导致的null值调用异常。
        return ReturnModel.success().setData(""+redisOps.get(RedisKeyConsistent.websocket_count.name()));
    }
    @PostMapping("/showBanUsers")
    public ReturnModel showBanUsers(@RequestAttribute(GatewayConsistent.USER_ID) Long userId) throws JsonProcessingException {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.showInfo.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        //实际上core模块维护在redis中的数据。这里的空字符串是为了防止redis中没有对应的键导致的null值调用异常。
        return ReturnModel.success().setData(objectMapper.writeValueAsString(banService.getAllBanUsers()));
    }
    @PostMapping("/banUser")
    public ReturnModel banUser(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestParam Long banUserId, @RequestParam Long liftTime) throws JsonProcessingException {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.Update.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        banService.save(new Ban(banUserId, Timestamp.from(Instant.ofEpochMilli(liftTime))));
        return ReturnModel.success().setData(objectMapper.writeValueAsString(banService.getAllBanUsers()));
    }

    @PostMapping("/updateBanUser")
    public ReturnModel updateBanUser(@RequestAttribute(GatewayConsistent.USER_ID) String userId, @RequestParam String banUserId, @RequestParam Long liftTime) throws JsonProcessingException {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(Long.valueOf(userId));
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.Update.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        // 如果liftTime早于当前时间，表示解除封禁
        if(liftTime < System.currentTimeMillis()){
            banService.removeById(banUserId);
        }else{
            //实际上core模块维护在redis中的数据。这里的空字符串是为了防止redis中没有对应的键导致的null值调用异常。
            banService.lambdaUpdate().eq(Ban::getId, banUserId).set(Ban::getLiftTime, liftTime).update();
        }
        return ReturnModel.success().setData(objectMapper.writeValueAsString(banService.getAllBanUsers()));
    }

    /**
     * 更新用户的权限
     * @param userId
     * @param updateUserId
     * @return
     */
    @PostMapping("/updateUserPermission")
    public ReturnModel updateUserPermission(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestParam Long updateUserId, @RequestParam int permission) {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.Update.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        userPermissionService.updateUserPermission(updateUserId, permission);
        Integer userNewPermission = userPermissionService.getUserPermission(updateUserId);
        return ReturnModel.success().setData(userNewPermission);
    }

    /**
     * 发送通知到任何人
     * @return
     */
    @PostMapping("/noticeSomeOne")
    public ReturnModel noticeSomeOne(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestParam Long noticeGoalUserId) {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.notice.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        //TODO: 需要配合消息通知系统，这里如果noticeGoalUserId为0 那么就会通知所有用户。
        return null;
    }
    /**
     * 发送邮件到任何人
     * @return
     */
    @PostMapping("/sendEmailSomeOne")
    public ReturnModel sendEmailSomeOne(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestParam Long noticeGoalUserId) {
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.notice.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        //TODO: 需要配合消息通知系统，这里如果noticeGoalUserId为0 那么就会通知所有用户。
        return null;
    }
    // --------------- 公开接口 --------------
    /**
     * 查询系统中的权限列表
     */
    @PostMapping("/getPermissionRegistry")
    public ReturnModel getPermissionRegistry() throws JsonProcessingException {
        List<PermissionRegistry> list = permissionRegistryService.list();
        return ReturnModel.success().setData(objectMapper.writeValueAsString(list));
    }
    /**
     * 查询用户自己所拥有的权限
     */
    @PostMapping("/getUserPermission")
    public ReturnModel getUserPermission(@RequestAttribute(GatewayConsistent.USER_ID) Long userId) throws JsonProcessingException {
        Integer userPermission = userPermissionService.getUserPermission(userId);
        return ReturnModel.success().setData(userPermission);
    }

    @PostMapping("/notice/all")
    public ReturnModel noticeAllUser(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestBody String noticeContent){
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionType.notice.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        // 调用noticeService的发送到全局的操作

        return ReturnModel.success();
    }

}
