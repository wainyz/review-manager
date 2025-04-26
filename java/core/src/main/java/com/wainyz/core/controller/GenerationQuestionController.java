package com.wainyz.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.manager.PromptManager;
import com.wainyz.core.pojo.domin.DeepSeekRequestDO;
import com.wainyz.core.service.UserFileService;
import com.wainyz.core.utils.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("/deepseek")
public class GenerationQuestionController {
    //-----------------1---------------------
    @Autowired
    private UserFileService userFileService;
    @Autowired
    private MessageSender messageSender;
    @Value("${prompt.questionNum:10}")
    private int questionNum;
    @Autowired
    private PromptManager promptManager;
    @Autowired
            private ObjectMapper objectMapper;
    Logger logger = LoggerFactory.getLogger(GenerationQuestionController.class);
    //=================1======================
    /**
     * 用户上传文件，保存到本地和数据库中
     * 并且调用deepseek，生成题目
     * @param file 文件
     * @param title 文件标题
     * @param userId 用户id
     * @param isPublish 是否公开
     * @return 返回结果
     */
    @PostMapping("/sendFile")
    public ReturnModel generationQuestion(@RequestParam("file") MultipartFile file,
                                          @RequestParam("title") String title,
                                          @RequestAttribute(GatewayConsistent.USER_ID) String userId,
                                          @RequestParam boolean isPublish

    ) {
        // 网关中已经将user解析，并将信息放入请求头中
        // 从请求头中获取用户名
        try {
            Long fileId = userFileService.saveFile(title, file, Long.valueOf(userId), isPublish ? 1 : 0);
            if (fileId != null){
                generateQuestion(userId, fileId.toString(), "", questionNum);
                return ReturnModel.success().setMessage("文件上传成功,题目生成中");
            }else{
                return ReturnModel.fail().setMessage("文件上传失败");
            }
        }catch (Exception e){
            return ReturnModel.fail().setMessage(e.getMessage()+"，可能的异常：参数值转换问题，文件保存问题");
        }
    }

    /**
     * 此方法与json格式耦合，注意
     * @param userId
     * @param fileId
     * @param data
     * @param questionNum
     * @return
     */
    @PostMapping("/generateQuestion")
    public ReturnModel generateQuestion(
            @RequestAttribute(GatewayConsistent.USER_ID) String userId,
            @RequestParam("file_id") String fileId,
            @RequestBody String data,
            @RequestParam("question_num") int questionNum
    ) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(data);
        String mustHave = jsonNode.get("mustHave").textValue();
        String otherPrompt = jsonNode.get("otherPrompt").textValue();
        //生成提示词
        DeepSeekRequestDO deepSeekRequestDO = promptManager.generateQuestionPrompt(questionNum, mustHave, otherPrompt, userId, fileId);
        messageSender.sendDeepSeekRequest(deepSeekRequestDO);
        return ReturnModel.success().setData("请求已提交。");
    }
}
