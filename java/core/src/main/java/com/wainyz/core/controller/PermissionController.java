package com.wainyz.core.controller;

import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.NoticeService;
import com.wainyz.user.controller.PermissionApiController;
import com.wainyz.user.service.PermissionRegistryService;
import com.wainyz.user.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {
    @Autowired
    private PermissionApiController permissionApiController;
    @Autowired
    private UserPermissionService userPermissionService;
    @Autowired
    private PermissionRegistryService permissionRegistryService;
    @Autowired
    private NoticeService noticeService;
    @PostMapping("/notice/all")
    public ReturnModel noticeAllUser(@RequestAttribute(GatewayConsistent.USER_ID) Long userId, @RequestParam String content){
        //权限校验
        Integer userPermission = userPermissionService.getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionApiController.PermissionType.notice.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return ReturnModel.fail().setMessage("没有权限。");
        }
        // 调用noticeService的发送到全局的操作
        Notice notice = Notice.build("-1", NoticeTypeEnum.SYSTEM_ALL, content);

        noticeService.sendNoticeToAllUser(notice);
        return ReturnModel.success();
    }
}
