package com.wainyz.core.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.core.analyizer.ControllerFileAnalyzer;
import com.wainyz.core.consident.AttenuationEnum;
import com.wainyz.core.manager.DataFileManager;
import com.wainyz.core.pojo.domin.ControllerFileDO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负责定时衰减每个掌握度分析文件中的题目的掌握度
 * @author Yanion_gwgzh
 */
//@Component
public class AttenuationTimedTasks {
    @Autowired
    private DataFileManager dataFileManager;
    @Autowired
    private ControllerFileAnalyzer controllerFileAnalyzer;
    private final Logger logger = LoggerFactory.getLogger(AttenuationTimedTasks.class.getName());
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            2,
            5,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );
    /**
     * 设置1天的定时任务，更新每个文件中不同题目的掌握度
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void attenuation(){
        //任务主体
        Runnable task = () -> {
            // 1 获取所有文件
            File[] files = dataFileManager.getAllControllerFile();
            // 2 遍历文件,并修改
            try {
                for (File file : files) {
                    if (file.isFile()) {
                        everyFileAttenuation(file);
                    }
                }
            }catch (Exception e){
                logger.error("AttenuationTimedTasks.attenuation() find exception: ", e);
            }
        };
        threadPoolExecutor.execute(task);
    }
    private final ObjectMapper objectMapper = new ObjectMapper();
    @PostConstruct
    private void init(){
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        // 设置更低的优先级,保证不影响业务逻辑
        threadPoolExecutor.setThreadFactory(r -> {
            Thread thread = new Thread(r, "AttenuationThread");
            thread.setDaemon(true);
            thread.setPriority(1);
            return thread;
        });
    }

    /**
     * 处理每一个文件的具体更新
     * @param file
     */
    private void everyFileAttenuation(File file) throws Exception {
       ControllerFileDO controllerFileDO;
        if (file.exists()) {
            // 做出变化
            controllerFileDO = controllerFileAnalyzer.getControllerFileDO(dataFileManager.getControllerFileContent(file));
            //遍历文件中的每个 record
            for (ControllerFileDO.MasteryRecordDO masteryRecord: controllerFileDO.getMasteryRecords()) {
                // 先做衰减
                // 获取衰减等级
                AttenuationEnum attenuationEnum = masteryRecord.getAttenuationLevel();
                int mastery = masteryRecord.getMastery();
                mastery *= 1-attenuationEnum.getAttenuation();
                //  warning: 这样做可能会出现 问题
                if(mastery < 0 || mastery > 100){
                    mastery = 0;
                }
                masteryRecord.setMastery(mastery);
                // 更新衰减等级
                updateAttenuationLevel(masteryRecord, attenuationEnum);
            }
            // 写入文件
            dataFileManager.saveControllerFile(file.getName().substring(0,file.getName().lastIndexOf(".")),
                    controllerFileDO.toJsonString());
        }
    }

    /**
     * 如果某个题目的掌握度低于60%，则可能被提高衰减等级。
     */
    private final int lowerMastery = 60;

    /**
     * 用于更新衰减等级
     * @param masteryRecord 记录
     */
    private void updateAttenuationLevel(ControllerFileDO.MasteryRecordDO masteryRecord, AttenuationEnum attenuationEnum){
        // 如果是0级，则不更新等级
        if (attenuationEnum.getLevel() == 0) {
            return;
        }
        // 检测是否达到时长
        if (masteryRecord.getFirstTime().plus(Duration.ofDays(attenuationEnum.getTime())).isBefore(Instant.now())) {
            // 检测是否掌握度低于 60%
            if ((masteryRecord.getMastery()) < lowerMastery) {
                // 如果达到，则更新等级,衰减等级+1
                masteryRecord.setAttenuationLevel(AttenuationEnum.getAttenuationEnum(
                        attenuationEnum.getLevel() +1
                ));
                // 并且重置时间
                masteryRecord.setFirstTime(Instant.now());
            }
        }
    }
}
