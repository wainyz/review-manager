package com.wainyz.core.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.analyizer.ControllerFileAnalyzer;
import com.wainyz.core.manager.DataFileManager;
import com.wainyz.core.manager.QuickStatusManager;
import com.wainyz.core.pojo.domain.ControllerFileDO;
import com.wainyz.core.pojo.po.UserFilePO;
import com.wainyz.core.pojo.vo.DocumentVo;
import com.wainyz.core.service.UserFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于查询文件的相关信息
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("/file_info")
public class QueryInfoController {
    //----------------1--------------
    private static final Logger logger = LoggerFactory.getLogger(QueryInfoController.class);
    @Autowired
    private UserFileService userFileService;
    @Autowired
    private ControllerFileAnalyzer controllerFileAnalyzer;
    @Autowired
    private DataFileManager dataFileManager;
    @Autowired
    private QuickStatusManager quickStatusManager;
    //=============1=======================
    @GetMapping("/file_count")
    ReturnModel queryFileCount(@RequestAttribute(GatewayConsistent.USER_ID) Long userId) {
        Long count = userFileService.lambdaQuery().eq(UserFilePO::getOwner, userId).count();
        return ReturnModel.success().setData(count);
    }
    @GetMapping("/docs")
    ReturnModel queryFileTitle(@RequestAttribute(GatewayConsistent.USER_ID) Long userId) {
        // 寻找所有的用户原文件
        List<UserFilePO> userFilePOList = userFileService.lambdaQuery().eq(UserFilePO::getOwner, userId).list();
        ArrayList<DocumentVo> result = new ArrayList<>(userFilePOList.size());
        userFilePOList.forEach(userFilePO -> {
            Long id = userFilePO.getId();
            String controllerFileContent = dataFileManager.getControllerFileContent(id.toString());
            ControllerFileDO controllerFileDO = null;
            try {
                controllerFileDO = controllerFileAnalyzer.getControllerFileDO(controllerFileContent);
            } catch (JsonProcessingException e) {
                logger.error("[57]controllerFileAnalyzer.getControllerFileDO(controllerFileContent) error", e);
                throw new RuntimeException(e);
            }
            DocumentVo documentVo = new DocumentVo();
            documentVo.setTitle(userFilePO.getTitle());
            documentVo.setId(id.toString());
            documentVo.setMastery(String.valueOf(controllerFileDO.getAllMastery()/100));
            documentVo.setCoverage(String.valueOf(controllerFileDO.getCoverage()/100));
            result.add(documentVo);
        });
        return ReturnModel.success().setData(result);
    }

    /**
     * 查询文件的分析文件
     * @param fileId
     * @return
     */
    @GetMapping("/controller_file")
    ReturnModel controllerFile(@RequestParam("file_id") Long fileId) {
        String controllerFileContent = dataFileManager.getControllerFileContent(String.valueOf(fileId));
        return ReturnModel.success().setData(controllerFileContent);
    }
    @GetMapping("/file_history")
    ReturnModel fileHistory(@RequestParam("file_id") Long fileId) {
        //进入history目录，寻找所有的答题记录
        dataFileManager.getHistoryFilesContent(String.valueOf(fileId));
        return ReturnModel.success().setData(dataFileManager.getHistoryFilesContent(String.valueOf(fileId)));
    }
    @GetMapping("/current_question")
    ReturnModel currentQuestion(@RequestParam("file_id") Long fileId) {
        dataFileManager.getCurrenQuestionContent(String.valueOf(fileId));
        return ReturnModel.success().setData(dataFileManager.getCurrenQuestionContent(String.valueOf(fileId)));
    }
    @GetMapping("/original_file")
    ReturnModel originalFile(@RequestParam("file_id") Long fileId) {
        dataFileManager.getDataFileContent(String.valueOf(fileId));
        return ReturnModel.success().setData(dataFileManager.getDataFileContent(String.valueOf(fileId)));
    }
    @GetMapping("/requestStatus")
    ReturnModel canReload(@RequestAttribute(GatewayConsistent.USER_ID) String userId) {
        if(quickStatusManager.getStatus(userId)){
            return ReturnModel.success().setData(true);
        }else{
            return ReturnModel.fail().setData(false);
        }
    }
    @GetMapping("/averageWaitTime")
    ReturnModel averageWaitTime() {
        return ReturnModel.success().setData(QuickStatusManager.average_response_time_seconds);
    }
}
