package com.wainyz.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.core.pojo.domain.ClassMessage;
import com.wainyz.core.service.ClassMessageService;
import com.wainyz.core.mapper.ClassMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Yanion_gwgzh
* @description 针对表【class_message】的数据库操作Service实现
* @createDate 2025-06-01 16:45:20
*/
@Service
public class ClassMessageServiceImpl extends ServiceImpl<ClassMessageMapper, ClassMessage>
    implements ClassMessageService{

}




