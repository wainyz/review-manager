package com.wainyz.core.pojo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;

/**
 * deepseek请求参数封装
 * @author Yanion_gwgzh
 */
@Data
public class DeepSeekRequestDO {

    @JsonProperty("systemPrompt")
    private String systemPrompt;

    @JsonProperty("userContent")
    private String userContent;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("messageId")
    private String messageId;
    // params  csv格式。
    @JsonProperty("params")
    private String params;

    @JsonProperty("requestEnum")
    private DeepSeekRequestEnum deepSeekRequestEnum;
    /**
     * 两中类型，一种生成题目，一种是评分
     */
    @Getter
    public enum DeepSeekRequestEnum{
        GENERATE_TEST(0,"测试题生成中..."),SCORING_ANSWERS(1,"批改题目中..."), GENERATE_PAPER(3, "试卷生成中...");
        @JsonValue
        private int typeCode;
        public String chineseName;

        DeepSeekRequestEnum(int typeCode, String chineseName) {
            this.typeCode = typeCode;
            this.chineseName = chineseName;
        }
        public static DeepSeekRequestEnum getEnum(int typeCode) {
            for (DeepSeekRequestEnum value : values()) {
                if (value.typeCode == typeCode) {
                    return value;
                }
            }
            return null;
        }
    }
}
