package com.wainyz.core.analyizer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wainyz.core.pojo.domain.QuestionFileDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于解析，重用questionFile的内容
 * {
 *   "questionNum":<生成的题目总数>,
 *   "questions":[
 *     {
 *       "raw": <出题依据的原文>,
 *       "question": <题目>,
 *       "correct": <参考答案>,
 *       "isNew":<true|false>
 *     },...
 *   ]
 * }
 * @author Yanion_gwgzh
 */
@Component
public class QuestionFileAnalyzer {
    //---------------------0------------------------
    private final ObjectMapper objectMapper;
    private final QuestionAnalyzer questionAnalyzer;
    public QuestionFileAnalyzer(@Autowired ObjectMapper objectMapper,
                                  @Autowired QuestionFileAnalyzer.QuestionAnalyzer questionAnalyzer) {
        this.objectMapper = objectMapper;
        this.questionAnalyzer = questionAnalyzer;
    }
    //======================0=================================
    //-----------------------1--------------------
    @Component
    public static class QuestionAnalyzer{
        private final ObjectMapper objectMapper;
        public QuestionAnalyzer(@Autowired ObjectMapper objectMapper
                                   ) {
            this.objectMapper = objectMapper;
        }
        public QuestionFileDO.QuestionDO  getQuestionDO(String jsonString) throws JsonProcessingException {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return getQuestionDO(jsonNode);
        }
        public QuestionFileDO.QuestionDO  getQuestionDO(JsonNode node) throws JsonProcessingException {
            String raw = node.get("raw").textValue();
            String question = node.get("question").textValue();
            String correct = node.get("correct").textValue();
            Integer recordIndex = node.get("recordIndex").intValue();
            return new QuestionFileDO.QuestionDO(raw, question, correct, recordIndex);
        }
    }
    //======================1====================
    public QuestionFileDO getQuestionFileDO(String jsonString) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        QuestionFileDO questionFileDO = new QuestionFileDO();
        questionFileDO.setQuestionNum(jsonNode.get("questionNum").intValue());
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("questions");
        QuestionFileDO.QuestionDO[] questions = new QuestionFileDO.QuestionDO[arrayNode.size()];
        questionFileDO.setQuestions(questions);
        int index = 0;
        for (JsonNode node: arrayNode){
            questions[index++] = questionAnalyzer.getQuestionDO(node);
        }
        return questionFileDO;
    }
}
