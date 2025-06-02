package com.wainyz.core.analyizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wainyz.core.consident.AttenuationEnum;
import com.wainyz.core.pojo.domain.ControllerFileDO;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;

/**
     * 用于解析，重用controllerFile的内容
{
 "coverage": <知识覆盖率 100>,
 "allMastery":<所有知识的总掌握度 100>,
 "currentMastery":<当前已出知识掌握度 100>,
 "masteryRecords":
 [
       {
         "raw": <出题依据的原文>,
         "mastery": <掌握度100分>,
         "attenuationLevel": <掌握度衰减等级>,
         "firstTime":<如果是第一次创建则记录下当前EpochSecond>
       },...
   ]
}
 * @author Yanion_gwgzh
 */
@Component
public class ControllerFileAnalyzer{
    //---------------------0------------------------
    private final ObjectMapper objectMapper;
    private final MasteryRecordAnalyzer masteryRecordAnalyzer;
    public ControllerFileAnalyzer(@Autowired ObjectMapper objectMapper,
                                  @Autowired MasteryRecordAnalyzer masteryRecordAnalyzer) {
        this.objectMapper = objectMapper;
        this.masteryRecordAnalyzer = masteryRecordAnalyzer;
    }
    //======================0=================================
    //---------------------1-------------------------------
    /*
    设置默认的MasteryRecordAnalyzer
     */
    @Component
    public static class MasteryRecordAnalyzer{
        private final ObjectMapper objectMapper;
        public MasteryRecordAnalyzer(@Autowired ObjectMapper objectMapper){
          this.objectMapper = objectMapper;
        }
        public ControllerFileDO.MasteryRecordDO getMasteryRecordDO(String jsonString) throws JsonProcessingException {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return getMasteryRecordDO(jsonNode);
        }
        public ControllerFileDO.MasteryRecordDO getMasteryRecordDO(JsonNode node) throws JsonProcessingException {
            ControllerFileDO.MasteryRecordDO masteryRecordDO = new ControllerFileDO.MasteryRecordDO();
            masteryRecordDO.setRaw(node.get("raw").textValue());
            masteryRecordDO.setMastery(node.get("mastery").intValue());
            masteryRecordDO.setAttenuationLevel(AttenuationEnum.getAttenuationEnum(node.get("attenuationLevel").intValue()));
            masteryRecordDO.setFirstTime(Instant.ofEpochSecond(node.get("firstTime").longValue()));
            return masteryRecordDO;
        }
    }
    //==================1===========================
    //-----------------2------------------------------
    /*
    主要提供的方法就是获取ControllerFileDO对象
     */
    public ControllerFileDO getControllerFileDO(String jsonString) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        ControllerFileDO controllerFileDO = new ControllerFileDO();
        int coverage =  jsonNode.get("coverage").intValue();
        int currentMastery = jsonNode.get("currentMastery").intValue();
        int allMastery = jsonNode.get("allMastery").intValue();
        ArrayNode masteryRecords = ((ArrayNode) jsonNode.get("masteryRecords"));
        ControllerFileDO.MasteryRecordDO[] masteryRecordsArray = new ControllerFileDO.MasteryRecordDO[masteryRecords.size()];
        int index = 0;
        for (JsonNode masteryRecord : masteryRecords){
            masteryRecordsArray[index++] = masteryRecordAnalyzer.getMasteryRecordDO(masteryRecord);
        }
        controllerFileDO.setCoverage(coverage);
        controllerFileDO.setCurrentMastery(currentMastery);
        controllerFileDO.setAllMastery(allMastery);
        controllerFileDO.setMasteryRecords(new ArrayList<>(Collections.arrayToList(masteryRecordsArray)));
        return controllerFileDO;
    }
    //====================2=============================
}