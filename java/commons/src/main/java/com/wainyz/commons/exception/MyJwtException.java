package com.wainyz.commons.exception;


import lombok.Getter;

/**
 * @author Yanion_gwgzh
 */
@Getter
public class MyJwtException extends Exception{
    public MyJwtExceptionEnum exceptionReason = MyJwtExceptionEnum.UNKNOWN_ERROR;
    public MyJwtException(MyJwtExceptionEnum jwtEnum) {
        super(jwtEnum.getMessage());
        this.exceptionReason = jwtEnum;
    }
    public MyJwtException(MyJwtExceptionEnum jwtEnum, String message) {
        super(message);
        this.exceptionReason = jwtEnum;
    }
    public MyJwtException(Exception e) {
        super(e);
    }
    public enum MyJwtExceptionEnum{
        EXPIRED_TOKEN("token已过期"),
        INVALID_TOKEN("token无效"),
        OTHER_ERROR("其他错误"),
        UNKNOWN_ERROR("未知错误");
        private String message;
        MyJwtExceptionEnum(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}
