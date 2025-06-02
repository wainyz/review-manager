package com.wainyz.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.core.pojo.domain.Friends;
import com.wainyz.core.service.FriendsService;
import com.wainyz.core.mapper.FriendsMapper;
import org.springframework.stereotype.Service;

/**
* @author Yanion_gwgzh
* @description 针对表【friends】的数据库操作Service实现
* @createDate 2025-05-18 09:36:24
*/
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends>
    implements FriendsService{

}




