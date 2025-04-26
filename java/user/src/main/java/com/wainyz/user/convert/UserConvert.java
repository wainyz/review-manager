package com.wainyz.user.convert;

import com.wainyz.commons.pojo.domin.UserDO;
import com.wainyz.commons.pojo.vo.UserVO;
import com.wainyz.user.pojo.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Yanion_gwgzh
 */
@Mapper
public abstract class UserConvert {
    public static UserConvert  INSTANCE = Mappers.getMapper(UserConvert.class);
    public abstract UserVO poTovo(UserPO userPO);
    public abstract UserDO poTodo(UserPO userPO);
    public abstract UserVO doTovo(UserDO userDO);
}
