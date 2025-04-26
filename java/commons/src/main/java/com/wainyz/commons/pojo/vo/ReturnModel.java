package com.wainyz.commons.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 后端返回给前端的统一模式
 * @author Yanion_gwgzh
 */
@Data
@Accessors(chain = true)
public class ReturnModel {
    public Integer code;
    public String message;
    public Object data;

    public static ReturnModel success(){
        return new ReturnModel().setCode(200);
    }
    public static ReturnModel fail(){
        return new ReturnModel().setCode(400);
    }
}
