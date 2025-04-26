package com.wainyz.core.pojo.domin;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wainyz.core.CoreApplication;
import com.wainyz.core.analyizer.QuestionFileAnalyzer;
import lombok.Data;

/**
 * @author Yanion_gwgzh
 */
@Data
public class QuestionFileDO {
    public static final ObjectMapper OBJECT_MAPPER = CoreApplication.OBJECT_MAPPER;
    private Integer questionNum;
    private QuestionDO[] questions;
//    public static QuestionFileDO build(String questionContent) throws JsonProcessingException {
//        return OBJECT_MAPPER.readValue(questionContent, QuestionFileDO.class);
//    }
    @Data
    public static class QuestionDO{
        private String raw;
        private String question;
        private String correct;
        private Integer recordIndex;
        public QuestionDO(){}
        public QuestionDO(String raw, String question, String correct, Integer recordIndex){
            this.raw = raw;
            this.question = question;
            this.correct = correct;
            this.recordIndex = recordIndex;
        }
    }
    public String toJsonString() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(this);
    }

    /**
     * 用于scoring所需要的 question信息
     * @return
     */
    public String[] justQuestionCorrectString(){
        String[] strings = new String[questions.length];
        for (int i = 0; i < questions.length; i++) {
            strings[i] = questions[i].question +"参考答案: "+ questions[i].correct;
        }
        return strings;
    }
}
