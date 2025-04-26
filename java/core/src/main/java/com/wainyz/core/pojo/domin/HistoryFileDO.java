package com.wainyz.core.pojo.domin;

import lombok.Data;
import java.time.Instant;
import static com.wainyz.core.CoreApplication.OBJECT_MAPPER;

/**
 * 用于组装历史文件, 设计一下json
 * 首先需要有question有correct，有answer，有scoring，有advice和similar
 *
 * @author Yanion_gwgzh
 */
@Data
public class HistoryFileDO {
    private Long time;
    private QuestionFileDO questions;
    private String[] userAnswer;
    private ScoringResponse scoringResult;
    public HistoryFileDO(QuestionFileDO questionFileDO, String[] userAnswer, ScoringResponse scoringResponse){
        this.time = Instant.now().getEpochSecond();
        questions = questionFileDO;
        this.userAnswer = userAnswer;
        this.scoringResult = scoringResponse;
    }
    public HistoryFileDO(){}
    public String toJsonString() throws Exception {
        return OBJECT_MAPPER.writeValueAsString(this);
    }
    public static HistoryFileDO fromJsonString(String jsonString) throws Exception {
        return OBJECT_MAPPER.readValue(jsonString, HistoryFileDO.class);
    }
}
