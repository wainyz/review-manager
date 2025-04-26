package com.wainyz.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wainyz.commons.pojo.domin.UserDO;
import com.wainyz.commons.pojo.vo.UserVO;
import com.wainyz.user.pojo.po.UserPO;
import org.springframework.stereotype.Service;

/**
 * @author Yanion_gwgzh
 */
@Service
public interface UserService extends IService<UserPO> {
    UserDO getUserById(Long id);
    UserDO getUserByEmail(String email);
    UserVO login(String email, String password);
    UserVO register(UserPO userVO);
    Boolean deleteUser(Long id);

    Boolean deleteUser(String email);
}
