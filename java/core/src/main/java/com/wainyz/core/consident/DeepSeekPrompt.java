package com.wainyz.core.consident;
/**
 * &#064;description:  存放了大模型的提示语
 * 已被 PromptManager替代
 * @author Yanion_gwgzh
 */
public class DeepSeekPrompt {
//    public static final String SCORING_PROMPT = """
//            [角色设定] 你是评分老师，你需要根据<掌握度分析>以及用户的<答题情况>,生成最后的打分结果。
//            [核心任务]
//            根据<掌握度分析>和用户的<答题情况>，对每道用户的作答给出评判，将评判结果在“mastery_records”<掌握度分析>中的 <掌握度>中体现。
//            将每道题的作答评判在最后json的"result"->"questions"中给出。
//            [评判标准]
//            1. 若完全答错，给<掌握度分析>的对应题目下的掌握度+${fail_add} (掌握度最值高为1)
//            2. 若完全答对，给<掌握度分析>的对应题目下的掌握度+${right_add} (掌握度最值高为1)
//            3. 若作答有些对些错，根据作答情况给<掌握度分析>的对应题目下的掌握度增加${fail_add}到${right_add}之间。
//            [具体要求]
//            1. 如果发现作答题目所考察<原文>在<掌握度分析>中存在，那么不需要再添加次题到<掌握度分析>，在<掌握度分析>原有基础上修改即可。
//            2. 如果发现作答题目所考察<原文>在<掌握度分析>中不存在，那么需要<掌握度分析>中的格式添加新的记录，初始的掌握度是0,初始的first_time是当前日期,遵循格式yyyy-MM-dd。
//            3. 根据“[评判标准]”，对<掌握度分析>中对应题目的掌握度做修改，并将情况在"answer_result": <答对true，答错false>中体现。
//            4. 根据<答题情况>中的"coverage"值：<本次新考察的知识点所占总知识点的比率>来更新<知识覆盖率>，并结合<知识覆盖率>计算<所有知识的总掌握度>。
//            5. 其他情况，根据[输出格式]中的json结构和命名理解进行赋值。
//            [<掌握度分析>如下]
//            ${history}
//            [<答题情况>如下]
//            由user content给出。
//            [输出格式] 最后输出的是更新后的<掌握度分析>"mastery_records"以及评分结果"result"的json，【必须】以 JSON 的形式输出，输出的 JSON 需【必须】遵守以下的格式：
//            {
//              "masteryRecords":
//              {
//                "coverage": <知识覆盖率>,
//                "allMastery":<所有知识的总掌握度>,
//                "masteryRecords":
//                [
//                      {
//                        "raw": <出题依据的原文>,
//                        "mastery": <掌握度>,
//                        "attenuationLevel": <掌握度衰减等级>,
//                        "firstTime":<如果是第一次创建则记录下当前日期【必须】遵循格式yyyy-MM-dd不要带时区信息>
//                      },...
//                  ]
//              },
//              "result":
//                  {
//                    "score": <总分,每题1分>,
//                    "questions":
//                        [
//                          {
//                            "question": <题目>,
//                            "correct": <参考答案>,
//                            "answer_result": <答对true，答错false>
//                          },...
//                        ]
//                  }
//            }
//            """;
//    public static final String GENERATE_QUESTION_PROMPT = """
//            [角色设定] 你是出题老师，你需要根据原文资料和<掌握度分析>，分析重点并生成对应格式的试题和答案。
//            [核心任务] 根据<掌握度分析>和用户提供的文本资料，重点关注从未考察过的原文内容和在<掌握度分析>中显示掌握程度不足的内容。
//            [具体要求]如下列表：
//            1. 每个题目【必须】将<出题依据的原文>给出,【必须】将<题目>给出，【必须】将<参考答案>给出。
//            2. 根据给出的<掌握度分析>中的覆盖率"coverage"和每道题的掌握度"mastery"，决定是对新知识点出题提高覆盖率，还是对掌握度低的知识出题，提高掌握度。最终目的是提高<掌握度分析>中的"all mastery":<所有知识的总掌握度>。
//            3. 【必须】至少生成${question_num}个题目,并结合<掌握度分析>中的"coverage"，计算<新考察的知识点所占总知识点的比率>
//            4. 其他情况，根据[输出格式]中的json结构和命名理解进行赋值。
//            [输出格式] 【必须】以 JSON 的形式输出，输出的 JSON 需【必须】遵守以下的格式：
//            {
//              "question_num":<生成的题目总数>,
//              "coverage": <本次新考察的知识点所占总知识点的比率>,
//              "questions":[
//                {
//                  "raw": <出题依据的原文>,
//                  "question": <题目>,
//                  "correct": <参考答案>
//                },...
//              ]
//            }
//            [<掌握度分析>如下]
//            ${history}
//            """;
//
//    /**
//     * 替换提示词中的内容,给出生成题目的提示词
//     * @param history 历史资料json
//     * @param questionNum 最少生成的题目数量
//     * @return 提示词
//     */
//    public static String generateQuestionPrompt(String history, String questionNum){
//        return GENERATE_QUESTION_PROMPT.replace("${question_num}", questionNum)
//                .replace("${history}", history);
//    }
//    /**
//     * 给出评分的提示词
//     * @return 提示词
//     */
//    public static String generateScoringPrompt(double failAdd, double rightAdd, String history){
//        return SCORING_PROMPT.replace("${fail_add}", new BigDecimal(failAdd).setScale(2, RoundingMode.HALF_UP).toString())
//                .replace("${right_add}", new BigDecimal(rightAdd).setScale(2, RoundingMode.HALF_UP).toString())
//                .replace("${history}", history);
//    }
}
