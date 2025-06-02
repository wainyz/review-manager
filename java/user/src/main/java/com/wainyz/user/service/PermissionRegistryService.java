package com.wainyz.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wainyz.user.pojo.po.PermissionRegistry;
import org.springframework.stereotype.Service;

@Service
public interface PermissionRegistryService extends IService<PermissionRegistry> {

    Boolean registryServicePermission(String serviceName, int permission);

    Integer getServicePermission(String serviceName);
    Boolean deleteServicePermission(String serviceName);
}
