package com.wainyz.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.pojo.domain.ExamParamDO;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class GenerationQuestionControllerTests {
    public static final Logger logger = LoggerFactory.getLogger(GenerationQuestionControllerTests.class);
    @Autowired
    private GenerationQuestionController controller;
    @Autowired
    private ObjectMapper objectMapper;

    String testData = """
                {
                "knowledgePointsAndScope":"高中物理，牛顿定律，运动方程，重力，电学，洛伦兹力，磁场等方面的内容",
                "distributionOfQuestionTypesAndPercentageOfScores":"10道选择题每题2分，20道填空题，5道解答题",
                "theNameOfTheExamPaper":"物理测验 2025/5/10",
                "theDifficultyOfTheExamPaper":"难度在平均分到60分左右的程度",
                "referenceDataForTheExamPaper":""
                }
            """;
    @Test
    public void testGenerationExam() throws JsonProcessingException {
        String userId = "1";
        ExamParamDO examParamDO;
        examParamDO = objectMapper.readValue(testData,ExamParamDO.class);
        assert examParamDO != null;
        System.out.println(examParamDO.toUserPrompt());
        ReturnModel returnModel = controller.generationExam(userId, examParamDO);
        System.out.println(returnModel);
    }
}
