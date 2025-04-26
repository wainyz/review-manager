package com.wainyz.core.analyizer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.core.consident.AttenuationEnum;
import com.wainyz.core.convert.QuestionAndMasteryRecordConverter;
import com.wainyz.core.manager.DataFileManager;
import com.wainyz.core.pojo.domin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 主要在处理deepseek的消息返回上，更新文件内容
 * @author yanion_gwgzh
 */
@Component
public class ReviewAnalyzer {
    //-------------0--------------
    @Autowired
    private DataFileManager dataFileManager;
    @Autowired
    private ControllerFileAnalyzer controllerFileAnalyzer;
    @Autowired
    private QuestionFileAnalyzer questionFileAnalyzer;
    //=============0==============
    //--------------1--------------

    /**
     * TODO 需要重点关注这个方法，是否计算出了合法的mastery和coverage
     * warning：如果更新生成的json格式，这个方法会失效
     * generate json :
     * {
     *   "questionNum":<生成的题目总数>,
     *   "questions":[
     *     {
     *       "raw": <出题依据的原文>,
     *       "question": <题目>,
     *       "correct": <参考答案>,
     *       "new":<true|false>
     *     },...
     *   ]
     * }
     * @param fileId
     * @param response
     * @throws Exception
     */
    public void analyzeGenerateQuestionResponse(String fileId, DeepSeekResponse response)throws Exception{
        // 更新controller file
        String controllerFileContent = dataFileManager.getControllerFileContent(fileId);
        ControllerFileDO controllerFileDO = controllerFileAnalyzer.getControllerFileDO(controllerFileContent);
        QuestionFileDO questionFileDO = questionFileAnalyzer.getQuestionFileDO(response.getResponse());
        //重设currentMastery
        controllerFileDO.setCurrentMastery(
                calculateControllerMastery(controllerFileDO.getCurrentMastery()*controllerFileDO.getMasteryRecords().size(),0,
                        controllerFileDO.getMasteryRecords().size()+questionFileDO.getQuestionNum())
        );
        QuestionFileDO.QuestionDO[] allQuestion = questionFileDO.getQuestions();
        for (QuestionFileDO.QuestionDO questionDO : allQuestion) {
            if (questionDO.getRecordIndex() == -1) {
                questionDO.setRecordIndex(controllerFileDO.getMasteryRecords().size());
            }
            //添加到controller文件中
            controllerFileDO.addMasteryRecord(QuestionAndMasteryRecordConverter.questionToMasteryRecord(questionDO));
        }
        // 保存current question
        dataFileManager.saveCurrenQuestionFile(fileId, questionFileDO.toJsonString());
        //计算coverage和allMastery
        controllerFileDO.setCoverage(
                calculateCoverage(controllerFileContent.length()+response.getResponse().length(), dataFileManager.getDataFileSize(fileId)));
        controllerFileDO.setAllMastery(calculateFileMastery(controllerFileDO.getCurrentMastery(), controllerFileDO.getCoverage()));
        //保存controller file
        dataFileManager.saveControllerFile(fileId, controllerFileDO.toJsonString());
    }
    //==============1==============
    //--------------2--------------
    /**
     * 根据
     */

    /**
     * 计算已练题目掌握度
     */
    private int calculateControllerMastery(int oldMasteryCount, int newMasteryCount, int endArraySize){

        return (oldMasteryCount+newMasteryCount)/(endArraySize+1);
    }
    private int calculateFileMastery(int mastery, int coverage){
        return coverage/100*mastery;
    }
    private int calculateCoverage(long controllerFileLength, long rawFileLength){
        return (int) (controllerFileLength/(rawFileLength+1)*100);
    }
    //==============2==============
    //--------------3--------------
    @Autowired
    ObjectMapper objectMapper;

    /**
     * warning: 注意这段代码，硬编码可能需要优化
     * 处理打分之后的响应，核心代码。
     * @param response
     * @throws JsonProcessingException
     */
    public void analyzeScoringResponse(DeepSeekResponse response) throws Exception {
        ScoringResponse scoringResponse = ScoringRecordAnalyzer.jsonNodeToScoringResponse(response.getResponse());
        QuestionFileDO questionFileDO = questionFileAnalyzer.getQuestionFileDO(dataFileManager.getCurrenQuestionContent(response.getId()));
        QuestionFileDO.QuestionDO[] questions = questionFileDO.getQuestions();
        ControllerFileDO controllerFileDO = controllerFileAnalyzer.getControllerFileDO(dataFileManager.getControllerFileContent(response.getId()));
        List<ControllerFileDO.MasteryRecordDO> masteryRecords = controllerFileDO.getMasteryRecords();
        int index = 0;
        int masteryCount = 0;
        for (int scoringRecord : scoringResponse.getScores()){
            //按照顺序找到 controllerFile中record
            Integer recordIndex = questions[index].getRecordIndex();
            ControllerFileDO.MasteryRecordDO masteryRecordDO = masteryRecords.get(recordIndex);
            // 增加mastery，首先找到question，然后找到recordIndex,然后更新mastery
            int addMastery = 0;
            addMastery += 80 * Math.max(scoringRecord, 20) /100;
            masteryRecordDO.setMastery(masteryRecordDO.getMastery()+ addMastery);
            masteryCount+=addMastery;
            index ++;
        }
        //最后更新衰减等级
        attenuationLevel(controllerFileDO);
        //更新 currentMastery和allMastery
        int i = calculateControllerMastery(controllerFileDO.getCurrentMastery() * controllerFileDO.getMasteryRecords().size(),
                masteryCount, controllerFileDO.getMasteryRecords().size());
        controllerFileDO.setCurrentMastery(i);
        controllerFileDO.setAllMastery(i*controllerFileDO.getCoverage()/100);

        // 记录做题历史
        try {
            // 开始保存历史
            String fileId = response.getId();
            String answerFileContent = dataFileManager.getAnswerFileContent(fileId);
            String[] answer = objectMapper.readValue(answerFileContent, String[].class);
            HistoryFileDO historyFileDO = new HistoryFileDO(questionFileDO,answer, scoringResponse);
            dataFileManager.saveHistoryQuestionFile(fileId, historyFileDO.toJsonString());

            //保存controller file
            dataFileManager.saveControllerFile(response.getId(), controllerFileDO);
        }catch (IOException e){
            throw new RuntimeException("answer等中间文件已被删除。[146]");
        }finally {
            dataFileManager.deleteAnswerFile(response.getId());
        }
    }

    /**
     * 判断是否需要更新衰减等级
     * @param masteryRecords
     */

    private void attenuationLevel(ControllerFileDO masteryRecords) {
        // 获取当前时间
        Instant now = Instant.now();
        //如果当前熟练度大于等于80，并且时间大于等级升级所需要的时间那么本次答题可以进行衰减等级降级。
        for (ControllerFileDO.MasteryRecordDO masteryRecord : masteryRecords.getMasteryRecords()) {
            AttenuationEnum attenuationEnum = masteryRecord.getAttenuationLevel();
            int mastery = masteryRecord.getMastery();
            if (masteryRecord.getFirstTime().plus(Duration.ofDays(attenuationEnum.getTime())).isBefore(now)) {
                if (mastery > 80) {
                    masteryRecord.setAttenuationLevel(AttenuationEnum.getAttenuationEnum(attenuationEnum.getLevel() - 1));
                    masteryRecord.setFirstTime(now);
                }
            }
        }
    }
}
