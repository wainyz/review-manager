package com.wainyz.core.pojo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Yanion_gwgzh
 */
@Data
public class DeepSeekResponse implements Serializable {

    private String id;
    private String userId;
    private String params;
    private String response;
    private DeepSeekRequestDO.DeepSeekRequestEnum deepSeekRequestEnum;
    // 方案1：添加默认构造器
    public DeepSeekResponse() {}

    // 方案2：标注带参构造器（推荐用于不可变对象）
    @JsonCreator
    public DeepSeekResponse(
            @JsonProperty("id") String id,
            @JsonProperty("userId") String userId,
            @JsonProperty("params") String params,
            @JsonProperty("response") String response,
            @JsonProperty("deepSeekRequestEnum") int deepSeekRequestEnum
    ) {
        this.userId = userId;
        this.params = params;
        this.id = id;
        this.response = response;
        this.deepSeekRequestEnum = DeepSeekRequestDO.DeepSeekRequestEnum.getEnum(deepSeekRequestEnum);
    }
}