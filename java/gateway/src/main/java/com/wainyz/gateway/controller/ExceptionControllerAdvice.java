package com.wainyz.gateway.controller;

import com.wainyz.user.exception.CheckException;
import com.wainyz.user.exception.LoginException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Yanion_gwgzh
 */
@ControllerAdvice
public class ExceptionControllerAdvice {
    // 捕获参数检查异常
    @ExceptionHandler(CheckException.class)
    @ResponseBody
    public ResponseEntity<String> handleIllegalArgumentException(CheckException ex) {
        return ResponseEntity.badRequest().body("非法参数: " + ex.getMessage());
    }
    // 捕获登录异常
    @ExceptionHandler(LoginException.class)
    @ResponseBody
    public ResponseEntity<String> handleIllegalArgumentException(LoginException ex) {
        return ResponseEntity.badRequest().body("登录失败: " + ex.getMessage());
    }

    // 捕获所有异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(500).body("服务器内部错误: " + ex.getMessage());
    }
}
