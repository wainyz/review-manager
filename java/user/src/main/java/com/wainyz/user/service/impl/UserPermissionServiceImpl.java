package com.wainyz.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.user.controller.PermissionApiController;
import com.wainyz.user.mapper.UserPermissionMapper;
import com.wainyz.user.pojo.po.UserPermission;
import com.wainyz.user.service.PermissionRegistryService;
import com.wainyz.user.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yanion_gwgzh
 */
@Service
public class UserPermissionServiceImpl extends ServiceImpl<UserPermissionMapper, UserPermission> implements UserPermissionService {

    @Autowired
    private PermissionRegistryService permissionRegistryService;
    @Override
    public Integer getUserPermission(Long userId) {
        UserPermission one = lambdaQuery().eq(UserPermission::getId, userId).one();
        if(one != null){
            return one.getPermission();
        }else{
            return 0;
        }
    }

    @Override
    public Boolean updateUserPermission(Long userId, int permission) {
        return lambdaUpdate().eq(UserPermission::getId, userId).set(UserPermission::getPermission, permission).update();
    }

    @Override
    public Boolean deleteUserPermission(Long userId) {
        return lambdaUpdate().eq(UserPermission::getId, userId).remove();
    }

    @Override
    public Boolean insertUserPermission(Long userId, int permission) {
        int insert = baseMapper.insert(new UserPermission(userId, permission));
        return insert != 0;
    }

    /**
     * 权限校验
     * @param userId
     * @param permissionType
     * @return
     */

    @Override
    public Boolean checkUserPermission(Long userId, PermissionApiController.PermissionType permissionType) {
        //权限校验
        Integer userPermission = getUserPermission(userId);
        Integer servicePermission = permissionRegistryService.getServicePermission(PermissionApiController.PermissionType.showInfo.name());
        if((userPermission&(1 << servicePermission)) == 0){
            return false;
        }else {
            return true;
        }
    }
}
