package com.wainyz.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.core.pojo.domain.FriendsMessage;
import com.wainyz.core.service.FriendsMessageService;
import com.wainyz.core.mapper.FriendsMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Yanion_gwgzh
* @description 针对表【friends_message】的数据库操作Service实现
* @createDate 2025-05-23 14:51:20
*/
@Service
public class FriendsMessageServiceImpl extends ServiceImpl<FriendsMessageMapper, FriendsMessage>
    implements FriendsMessageService{

}




