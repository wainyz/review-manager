package com.wainyz.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.CoreApplication;
import com.wainyz.core.analyizer.QuestionFileAnalyzer;
import com.wainyz.core.manager.DataFileManager;
import com.wainyz.core.manager.PromptManager;
import com.wainyz.core.pojo.domin.QuestionFileDO;
import com.wainyz.core.utils.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

;import java.io.IOException;
import java.util.Arrays;

/**
 * 用于接受前端的打分请求
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("/scoring")
public class ScoringAnswersController {
    @Autowired
    private MessageSender messageSender;
    @Autowired
    private PromptManager promptManager;
    @Autowired
    private DataFileManager dataFileManager;
    @Autowired
    private QuestionFileAnalyzer questionFileAnalyzer;
    public static final ObjectMapper OBJECT_MAPPER = CoreApplication.OBJECT_MAPPER;

    @PostMapping("/answers")
    public ReturnModel scoringAnswer(@RequestParam("answer") String[] answer,
                                     @RequestAttribute(GatewayConsistent.USER_ID) String userId,
                                     @RequestParam("file_id")   String fileId) {
        //用户直接上传 raw+answer的json 数组即可。注意上传的数目和顺序必须和原顺序一样。
        //这样 可以直接发给deepseek，然后处理 response的时候直接通过顺序确定raw，然后通过raw确定controllerRecord进行更新即可。
        try {
            dataFileManager.saveAnswerFile(fileId, OBJECT_MAPPER.writeValueAsString(answer));
            String[] questionAndCorrects = questionFileAnalyzer.getQuestionFileDO(dataFileManager.getCurrenQuestionContent(fileId)).justQuestionCorrectString();
            if (messageSender.sendDeepSeekRequest(promptManager.scoringAnswerPrompt(Arrays.toString(questionAndCorrects),Arrays.toString(answer), userId, fileId))) {
                // 保存answer
                return ReturnModel.success().setMessage("提交成功");
            } else {
                dataFileManager.deleteAnswerFile(fileId);
                return ReturnModel.fail().setMessage("提交失败");
            }
        }catch (IOException e){
            dataFileManager.deleteAnswerFile(fileId);
            return ReturnModel.fail().setMessage(e.getMessage());
        }
    }
}
