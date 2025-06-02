package com.wainyz.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParseException;
import com.wainyz.core.mapper.ExamMapper;
import com.wainyz.core.pojo.domain.DeepSeekResponse;
import com.wainyz.core.pojo.po.Exam;
import com.wainyz.core.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author Yanion_gwgzh
* @description 针对表【exam】的数据库操作Service实现
* @createDate 2025-05-15 09:23:35
*/
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam>
    implements ExamService{
    private Logger logger = LoggerFactory.getLogger(ExamService.class);
    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 分解并存储DeepseekResponse
     * @param message
     * @return
     */
    @Override
    public boolean saveExam(DeepSeekResponse message) {
        String description = "";
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getResponse());
            description = jsonNode.get("description").asText();
        }catch (JsonParseException |
                JsonProcessingException e){
            logger.error("[error 39]: json 解析失败。");
            e.printStackTrace();
        }

        Exam exam = new Exam();
        try {
            exam.setOwnerId(Long.valueOf(message.getUserId()));
            exam.setTitle(description);
            exam.setDescription(description);
            exam.setTeachers(Long.valueOf(message.getUserId()));
        }catch (NumberFormatException e){
            logger.error("[error 33]: {}", "类型专转换错误。");
            e.printStackTrace();
        }
        return save(exam);
    }
}




