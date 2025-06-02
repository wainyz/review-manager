package com.wainyz.core.convert;

import com.wainyz.core.consident.AttenuationEnum;
import com.wainyz.core.pojo.domain.ControllerFileDO;
import com.wainyz.core.pojo.domain.QuestionFileDO;

import java.time.Instant;

/**
 * 用于转换 question 和 MasteryRecord
 * @author Yanion_gwgzh
 */
public class QuestionAndMasteryRecordConverter {
    public static ControllerFileDO.MasteryRecordDO questionToMasteryRecord(QuestionFileDO.QuestionDO questionDO){
        ControllerFileDO.MasteryRecordDO masteryRecordDO = new ControllerFileDO.MasteryRecordDO();
        masteryRecordDO.setFirstTime(Instant.now());
        masteryRecordDO.setMastery(0);
        masteryRecordDO.setRaw(questionDO.getRaw());
        masteryRecordDO.setAttenuationLevel(AttenuationEnum.Leve5);
        return masteryRecordDO;
    }
}
