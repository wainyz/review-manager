package com.wainyz.core.pojo.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wainyz.core.CoreApplication;
import com.wainyz.core.Serializer.InstantToTimestampSerializer;
import com.wainyz.core.consident.AttenuationEnum;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件的掌握分析对象
 * @author Yanion_gwgzh
 */
@Data
public class ControllerFileDO {
    private static final ObjectMapper OBJECT_MAPPER = CoreApplication.OBJECT_MAPPER;
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerFileDO.class);
    public static final String DEFAULT_CONTROLLER_FILE_CONTENT = """
            {
            "coverage": 0,
            "allMastery": 0,
            "currentMastery":0,
            "masteryRecords": []
            }
            """;

    @JsonProperty("coverage")
    private int coverage;
    @JsonProperty("allMastery")
    private int allMastery;
    @JsonProperty("currentMastery")
    private int currentMastery;
    @JsonProperty("masteryRecords")
    private List<MasteryRecordDO> masteryRecords;
    @Data
    public static class MasteryRecordDO{
        private String raw;
        private int mastery;
        private AttenuationEnum attenuationLevel;
        @JsonSerialize(using = InstantToTimestampSerializer.class)
        private Instant firstTime;
        public MasteryRecordDO(){}
        public void setMastery(int mastery){
            if(mastery <0){
                mastery = 0;
            }else if (mastery > 100){
                mastery = 100;
            }
            this.mastery = mastery;
        }
    }
    public void setAllMastery(int allMastery){
        if(allMastery <0){
            allMastery = 0;
        }else if (allMastery > 100){
            allMastery = 100;
        }
        this.allMastery = allMastery;
    }
    public void setCurrentMastery(int currentMastery){
        if(currentMastery <0){
            currentMastery = 0;
        }else if (currentMastery > 100){
            currentMastery = 100;
        }
        this.currentMastery = currentMastery;
    }
    public void setCoverage(int coverage){
        if(coverage <0){
            coverage = 0;
        }else if (coverage > 100){
            coverage = 100;
        }
        this.coverage = coverage;
    }
    public void addMasteryRecord(MasteryRecordDO recordDO){
        try {
            this.masteryRecords.add(recordDO);
        }catch (Exception e){
            this.masteryRecords = new ArrayList<>();
            this.masteryRecords.add(recordDO);
        }
    }
    public String toJsonString() throws Exception {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
             LOGGER.error("error 转换失败！[62]");
             throw new Exception("类型格式错误，请重新设计。");
        }
    }

}

