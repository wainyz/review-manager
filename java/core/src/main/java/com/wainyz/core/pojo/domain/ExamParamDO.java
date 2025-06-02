package com.wainyz.core.pojo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Yanion_gwgzh
 */
@Data
public class ExamParamDO {
    public static final String userPromptModel = """
[出题内容和范围限定]
${1}
[出题题型分布和分数占比]
${2}
[试卷难度描述]
${3}
[考题资料]
${4}
""";
    @JsonProperty("knowledgePointsAndScope")
    private String knowledgePointsAndScope;
    @JsonProperty("distributionOfQuestionTypesAndPercentageOfScores")
    private String distributionOfQuestionTypesAndPercentageOfScores;
    @JsonProperty("theNameOfTheExamPaper")
    private String theNameOfTheExamPaper;
    @JsonProperty("theDifficultyOfTheExamPaper")
    private String theDifficultyOfTheExamPaper;
    @JsonProperty("referenceDataForTheExamPaper")
    private String referenceDataForTheExamPaper;

    /**
     * 输出exam的用户prompt提示词。
     * @return
     */
    public final String toUserPrompt(){
        return userPromptModel.replace("${1}", knowledgePointsAndScope)
                .replace("${2}", distributionOfQuestionTypesAndPercentageOfScores)
                .replace("${3}", theDifficultyOfTheExamPaper)
                .replace("${4}", referenceDataForTheExamPaper);
    }
}
