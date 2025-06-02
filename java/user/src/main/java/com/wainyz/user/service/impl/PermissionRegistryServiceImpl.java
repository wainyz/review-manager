package com.wainyz.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.user.mapper.PermissionRegistryMapper;
import com.wainyz.user.pojo.po.PermissionRegistry;
import com.wainyz.user.service.PermissionRegistryService;
import org.springframework.stereotype.Service;

/**
 * @author Yanion_gwgzh
 */
@Service
public class PermissionRegistryServiceImpl extends ServiceImpl<PermissionRegistryMapper, PermissionRegistry> implements PermissionRegistryService {

    @Override
    public Boolean registryServicePermission(String serviceName, int permission) {
        PermissionRegistry permissionRegistry = new PermissionRegistry();
        permissionRegistry.setId(serviceName);
        permissionRegistry.setPermission(permission);
        removeById(serviceName);
        int insert = baseMapper.insert(permissionRegistry);
        return insert!=0;
    }

    @Override
    public Integer getServicePermission(String serviceName) {
        return lambdaQuery().eq(PermissionRegistry::getId, serviceName).one().getPermission();
    }

    @Override
    public Boolean deleteServicePermission(String serviceName) {
        return null;
    }
}
