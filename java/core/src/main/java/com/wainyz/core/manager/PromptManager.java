package com.wainyz.core.manager;


import com.wainyz.core.pojo.domain.DeepSeekRequestDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 用于生成大模型需要的提示词
 * @author Yanion_gwgzh
 */
@Component
public class PromptManager {
    //--------------0---------------
    public static final String PAPER_GENERATE_PROMPT = """
[角色设定]
你是一个出题老师，你需要根据<考题资料>,<出题模式>,<考察范围比例>来制作考题。
[核心任务]
首先读取<考题资料>如果发现不存在，请使用联网功能从网络中获取相关内容作为考题资料。
根据出题模式以及考察范围，合理安排题目的类型，考察的知识，题目的分数，以及考题整体题目的数量，确保最后试卷满分为100分。
[具体要求]
1. 总分必须100分，合理设置每道题的分值.
2. 合理安排题目的类型和考察的知识，做到符合<考察范围比例>以及<出题模式>
3. 合理寻找网络资料，保证考察内容不重复冗余，特别对于解答题，需要有完整合理的逻辑，更应该从网上搜索。
4. 不要省略，要求完整的题目列表。
5. 最终输出为json字符串，严格按照格式要求 输出。
[输出格式]
题目分为 选择题、填空题、解答题。其题目格式如下：
选择题：
{
    "question": "xxx",
    "chooseA": "A. xxx",
    "chooseB": "B. xxx",
    "chooseC": "C. xxx",
    "chooseD": "D. xxx",
    "rightChoose": "B",
    "score": <分值>
}
填空题:
{
     "question":"xxx${blank_1}xxx${blank_1}...",
     "rightAnswer":"${answer_1};&;${answer_2};&;...",
     "score": <分值>
}
解答题:
{
     "question":"xxx",
     "rightAnswer":"xxx",
     "score": <分值>
}
[最终输出]要求输出json字符串格式如下
{
    "description":"出题考察的类型和范围描述",
    "questionBlocks":[
        {
            "questionType": <选择题为1，填空题赋2，解答题赋3>,
            "questionsTitle": "题目类型描述和解释",
            "questions":[
                {
                    <根据具体题目的类型决定>
                }, ...
             ]
        }, ...
    ]
}
""";
    public static final String GENERATE_QUESTION_PROMPT =
"""
[角色设定]
你是出题老师，你需要根据<原文资料>、<掌握度分析>、以及<必出内容>，生成对应数量、格式的试题和答案。
[核心任务]
首先读取全部<原文资料>，然后读取必出内容，将涉及到的内容全部生成题目,及时超过规定题目数量也必须针对出题。
然后读取掌握度分析，分析目前掌握度很低的一些内容进行针对出题。
另外，找出掌握度分析中未曾涉及的原文知识，进行出题。
[注意]
必出内容、掌握度分析、额外指示 都可能不会给出，具有空内容，当发现时请忽略。
[具体要求]如下列表：
1.每个题目【必须】将<出题依据的原文>给出,【必须】将<题目>给出，【必须】将<参考答案>给出。
2.读取<掌握度分析>中的覆盖率"coverage"和每道题的掌握度"mastery"，并做出如下判断
    - 如果覆盖率coverage很低，那么优先针对掌握度分析中未出现的原文出题。
    - 如果掌握度mastery很低，那么优先对掌握度低的知识出题。
    - 如果两者都很低，请先照顾掌握度低的知识，其次才是未出现的知识。
3.【至少】生成${question_num}个题目
4.【必须】遵守recordIndex值的赋予办法
    - 如果题目所引raw从未出现在<掌握度分析>中，则recordIndex值给-1
    - 如果已在<掌握度分析>中 recordIndex值给raw在masteryRecords中的索引值
5. 根据[输出格式]中的json结构和命名理解进行赋值。
[输出格式]
【必须】以 JSON 的形式输出，输出的 JSON 需【必须】遵守以下的格式：
{
  "questionNum":<生成的题目总数>,
  "questions":[
    {
      "raw": <出题依据的原文>,
      "question": <题目>,
      "correct": <参考答案>,
      "recordIndex":<在 掌握度分析 中的索引值>
    },...
  ]
}
[必出内容]
必出内容在user role的content中给出。
[<原文资料>如下]
${raw_content}
[<掌握度分析>如下]
${controller_content}
[额外指示如下]
${other_prompt}
""";
    public static final String SCORING_ANSWER_PROMPT =
"""
[角色设定]
你是评分老师，你只需要根据用户的<答题情况>,以及参考答案给出打分结果即可。
[核心任务]
在打分之后，给出你的改进建议，一些相关知识。
[具体要求]
1. 请尽可能的快速打分。
2. 每道题的<分数>必须是 0 到 100分。
3. 必须使用中文回答。
4. 注意json格式要压缩、转义，保证可以被正常解析。
[<答题情况>如下]
由user content给出。
[输出格式]
【必须】以 JSON 的形式输出，输出的 JSON 需【必须】遵守以下的格式：
{
"scores":
    [
        <分数>,
        ...
    ],
"advice": <改进建议>,
"similar":<相关知识>
}
""";
    public static final String CONTROLLER_JSON_MODEL = """
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
        "firstTime":<如果是第一次创建则记录下当前日期>
      },...
  ]
}
""";
    /**
     * 默认生成的题目数量
     */
    @Value("${deepseek.generate_number:15}")
    private String defaultGenerateNumber = "15";
    @Autowired
    private DataFileManager dataFileManager;
    public PromptManager(){

    }
    //=================0============
    //----------------0.5-----------

    //================0.5===========
    //---------------1---------------

    /**
     * 生成用于生成题目的提示词
     */
    public DeepSeekRequestDO generateQuestionPrompt(int generateNumber,
                                                    String mustHave,
                                                    String otherPrompt,
                                                    String userId,
                                                    String fileId){
        String rawContent = "";
        rawContent = dataFileManager.getDataFileContent(fileId);
        if (rawContent == null){
            throw new RuntimeException("复习资料不存在");
        }
        // 校验数据合法性，并进行非法处理
        if(generateNumber < 1){
            generateNumber = Integer.parseInt(defaultGenerateNumber);
        }
        if(mustHave == null){
            mustHave = "没有要求";
        }
        if(otherPrompt == null){
            otherPrompt = "没有要求";
        }
        String controllerContent = dataFileManager.getControllerFileContent(fileId);
        String prompt = GENERATE_QUESTION_PROMPT.replace("${question_num}", String.valueOf(generateNumber))
                .replace("${other_prompt}", otherPrompt)
                .replace("${raw_content}", rawContent)
                .replace("${controller_content}", controllerContent);
        DeepSeekRequestDO deepSeekRequestDO = new DeepSeekRequestDO();
        deepSeekRequestDO.setSystemPrompt(prompt);
        deepSeekRequestDO.setUserContent(mustHave);
        deepSeekRequestDO.setUserId(userId);
        deepSeekRequestDO.setParams(fileId);
        deepSeekRequestDO.setDeepSeekRequestEnum(DeepSeekRequestDO.DeepSeekRequestEnum.GENERATE_TEST);
        return deepSeekRequestDO;
    }

    /**
     * 生成用于评分答案的提示词
     * @return
     */
    public DeepSeekRequestDO scoringAnswerPrompt(
            String questions,
            String answer,
            String userId,
            String fileId
    ){
        DeepSeekRequestDO deepSeekRequestDO = new DeepSeekRequestDO();
        deepSeekRequestDO.setSystemPrompt(SCORING_ANSWER_PROMPT);
        deepSeekRequestDO.setUserContent("{questions: "+questions+", answer: "+answer+"}");
        deepSeekRequestDO.setUserId(userId);
        deepSeekRequestDO.setParams(fileId);
        deepSeekRequestDO.setDeepSeekRequestEnum(DeepSeekRequestDO.DeepSeekRequestEnum.SCORING_ANSWERS);
        return deepSeekRequestDO;
    }
    // =================1============
}
