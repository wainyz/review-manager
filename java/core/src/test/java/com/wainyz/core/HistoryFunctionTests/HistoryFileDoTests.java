package com.wainyz.core.HistoryFunctionTests;

import com.wainyz.core.analyizer.QuestionFileAnalyzer;
import com.wainyz.core.manager.DataFileManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 *  测试 HistoryFileDO 类，构建，输出，转换
 */
@SpringBootTest
@ActiveProfiles("test")
public class HistoryFileDoTests {
    @Autowired
    DataFileManager dataFileManager;
    @Autowired
    QuestionFileAnalyzer questionFileAnalyzer;
    /**
     * 测试构建
     */
    @Test
    public void testBuild() throws Exception {
//        String fileId = "1908132350760460288";
//        QuestionFileDO questionFileDO = questionFileAnalyzer.getQuestionFileDO(dataFileManager.getCurrenQuestionContent(fileId));
//        String[] answer = {"不知道", "unknown", "right", "left", "-r"};
//        ScoringResponse scoringResponse = new ScoringResponse();
//        HistoryFileDO historyFileDO = new HistoryFileDO(questionFileDO, answer, scoringResponse);
//        HistoryFileDO historyFileDO1 = HistoryFileDO.fromJsonString(historyFileDO.toJsonString());
//        System.out.println(historyFileDO1.toJsonString());
//        assert historyFileDO1.getTime() != null;
    }
}
