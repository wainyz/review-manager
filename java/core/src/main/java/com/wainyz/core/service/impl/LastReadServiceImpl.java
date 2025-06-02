package com.wainyz.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.core.pojo.domain.LastRead;
import com.wainyz.core.service.LastReadService;
import com.wainyz.core.mapper.LastReadMapper;
import org.springframework.stereotype.Service;

/**
* @author Yanion_gwgzh
* @description 针对表【last_read】的数据库操作Service实现
* @createDate 2025-05-13 14:42:26
*/
@Service
public class LastReadServiceImpl extends ServiceImpl<LastReadMapper, LastRead>
    implements LastReadService{

}




