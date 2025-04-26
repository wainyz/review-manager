package com.wainyz.user.service;

import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.pojo.domin.UserRegistryDO;
import com.wainyz.user.service.impl.RegisterServiceImpl;

/**
 * @author Yanion_gwgzh
 */
public interface RegisterService {
    /**
     * 用于注册
     *
     * @param model
     * @return
     */
    ReturnModel registry(UserRegistryDO model) throws CheckException;

    /**
     * 用于检查邮箱是否注册
     * @param email
     * @return
     */

    ReturnModel sendEmail(String email, RegisterServiceImpl.SendReason reason) throws CheckException;
    boolean deleteUser(String id);
}
