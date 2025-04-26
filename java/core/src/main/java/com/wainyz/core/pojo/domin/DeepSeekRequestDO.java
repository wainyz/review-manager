package com.wainyz.core.pojo.domin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

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

    @JsonProperty("fileId")
    private String fileId;

    @JsonProperty("requestEnum")
    private DeepSeekRequestEnum deepSeekRequestEnum;
    /**
     * 两中类型，一种生成题目，一种是评分
     */
    @Getter
    public enum DeepSeekRequestEnum{
        GENERATE_QUESTION(0),SCORING_ANSWERS(1);
        @JsonValue
        private int typeCode;

        DeepSeekRequestEnum(int typeCode) {
            this.typeCode = typeCode;
        }
    }
}
