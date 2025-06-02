package com.wainyz.user.utils;

import com.wainyz.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @author Yanion_gwgzh
 */
@Component
public class UsernameGenerator {
    @Autowired
    private UserService userService;
    /**
     * 生成唯一的username，可能会冲突，需要输入冲突的username进行改进
     * 这里的思路是 2位年+邮箱前3位+邮箱验证码四位or6位+3位当前时刻后3位。如果冲突会自动递增后3位寻找未冲突的。
     * @return
     */
    public String generateUsername(String email, String emailCode ){
        StringBuilder builder = new StringBuilder().append(LocalDate.now().getYear()%100);
        builder.append(email, 0, 3);
        builder.append(emailCode).append(System.nanoTime()%1000);
        while (userService.findUsername(builder.toString())){
            builder.delete(builder.length()-4,builder.length()-1);
            builder.append(System.nanoTime()%1000);
        }
        return builder.toString();
    }
}
