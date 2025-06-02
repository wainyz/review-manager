package com.wainyz.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.commons.pojo.domin.UserDO;
import com.wainyz.commons.pojo.vo.UserVO;
import com.wainyz.user.convert.UserConvert;
import com.wainyz.user.mapper.UserMapper;
import com.wainyz.user.pojo.po.UserPO;
import com.wainyz.user.service.UserService;
import com.wainyz.user.utils.MyBCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author Yanion_gwgzh
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {
    @Autowired
    private MyBCryptPasswordEncoder encoder;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Override
    public UserDO getUserById(Long id) {
        UserPO one = lambdaQuery().eq(UserPO::getUserId, id).one();
        return UserConvert.INSTANCE.poTodo(one);
    }

    @Override
    public UserDO getUserByEmail(String email) {
        UserPO one = lambdaQuery().eq(UserPO::getEmail, email).one();
        return UserConvert.INSTANCE.poTodo(one);
    }

    @Override
    public UserVO login(String email, String password) {
        UserPO user = lambdaQuery().eq(UserPO::getEmail, email).one();
        if(user == null){
            user = lambdaQuery().eq(UserPO::getUsername,email).one();
        }
        if(user!=null && encoder.matches(password, user.getPassword())) {
            return UserConvert.INSTANCE.poTovo(user);
        } else {
            return null;
        }
    }

    @Override
    public UserVO register(UserPO user) {
        // 插入数据
        try {
            save(user);
        }catch (Exception e){
            logger.warn(e.getMessage());
            e.printStackTrace();
            return null;
        }
        return UserConvert.INSTANCE.poTovo(user);
    }

    @Override
    public Boolean deleteUser(Long id) {
        return removeById(id);
    }
    @Override
    public Boolean deleteUser(String email) {
        return baseMapper.delete(lambdaQuery().eq(UserPO::getEmail,email).getWrapper()) > 0;
    }

    @Override
    public Boolean findUsername(String username) {
        return baseMapper.selectCount(new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)) != 0;
    }
}
