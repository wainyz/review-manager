package com.wainyz.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wainyz.user.controller.PermissionApiController;
import com.wainyz.user.pojo.po.UserPermission;
import org.springframework.stereotype.Service;

@Service
public interface UserPermissionService extends IService<UserPermission> {
    Integer getUserPermission(Long userId);
    Boolean updateUserPermission(Long userId, int permission);
    Boolean deleteUserPermission(Long userId);
    Boolean insertUserPermission(Long userId, int permission);
    Boolean checkUserPermission(Long userId, PermissionApiController.PermissionType permissionType);
}
