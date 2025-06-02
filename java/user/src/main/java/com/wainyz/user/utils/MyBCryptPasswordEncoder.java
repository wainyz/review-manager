package com.wainyz.user.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用于加密和解密密码
 * @author Yanion_gwgzh
 */
@Component
public class MyBCryptPasswordEncoder extends BCryptPasswordEncoder {
    public MyBCryptPasswordEncoder(){
        // 强度参数（4-31，默认10）
        super(12);
    }
}
