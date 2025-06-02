package com.wainyz.core.service;

import com.wainyz.core.pojo.domain.DeepSeekResponse;
import com.wainyz.core.pojo.po.Exam;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Yanion_gwgzh
* @description 针对表【exam】的数据库操作Service
* @createDate 2025-05-15 09:23:35
*/
public interface ExamService extends IService<Exam> {

    boolean saveExam(DeepSeekResponse message);
}
