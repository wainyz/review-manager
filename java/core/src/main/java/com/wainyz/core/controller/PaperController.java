package com.wainyz.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.manager.PromptManager;
import com.wainyz.core.pojo.domain.DeepSeekRequestDO;
import com.wainyz.core.pojo.domain.Paper;
import com.wainyz.core.service.PaperService;
import com.wainyz.core.service.UserFileService;
import com.wainyz.core.service.WebSocketMessageService;
import com.wainyz.core.utils.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/paper")
public class PaperController {
    //-----------------1---------------------
    @Autowired
    private UserFileService userFileService;
    @Autowired
    private MessageSender messageSender;
    @Autowired
    private PromptManager promptManager;
    @Autowired
    private PaperService paperService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebSocketMessageService webSocketMessageService;
    Logger logger = LoggerFactory.getLogger(PaperController.class);

    //=================1======================
    /**
     *  生成试卷API，post /paper/generate 携带json参数，包括，直接传递给deepseek。
     * <p>
     *  return 试卷id 以及 平均等待时间
     */
    @PostMapping("/generate")
    public ReturnModel generationPaper(
            @RequestAttribute(GatewayConsistent.USER_ID) String userId,
            @RequestParam("paperConfig") String paperConfigJson
    ) throws JsonProcessingException {
        // 1 构建deepseek请求对象
        String generateQuestionAutoPrompt = PromptManager.PAPER_GENERATE_PROMPT;
        DeepSeekRequestDO deepSeekRequestDO = new DeepSeekRequestDO();
        deepSeekRequestDO.setUserId(userId);
        deepSeekRequestDO.setSystemPrompt(generateQuestionAutoPrompt);
        deepSeekRequestDO.setUserContent(paperConfigJson);
        deepSeekRequestDO.setDeepSeekRequestEnum(DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_PAPER);
        String title = objectMapper.readTree(paperConfigJson).get("name").textValue();
        deepSeekRequestDO.setParams(title);
        // 2 发送deepseek请求
        messageSender.sendDeepSeekRequest(deepSeekRequestDO);
        return ReturnModel.success();
    }
    /**
     * 获取所有自己的试卷
     */
    @GetMapping("/list")
    public ReturnModel getPaperList(@RequestAttribute(GatewayConsistent.USER_ID) String userId){
        List<Paper> list = paperService.lambdaQuery().eq(Paper::getUserId, userId).list();
        return ReturnModel.success().setData(list);
    }
    @PostMapping("/update")
    public ReturnModel generationPaper(
            @RequestAttribute(GatewayConsistent.USER_ID) String userId,
            @RequestParam("paperId") String paperId,
             @RequestParam("title" ) String title,
            @RequestParam("content") String content
    ) {
        // 生成提示词
        boolean update = paperService.lambdaUpdate().set(Paper::getContent, content)
                .set(Paper::getTitle, title)
                .eq(Paper::getId, paperId)
                .eq(Paper::getUserId, userId).update();
        if ( update){
             return ReturnModel.success().setMessage("更新成功.");
        }else{
            return ReturnModel.fail().setMessage("更新失败.");
        }
    }
}
