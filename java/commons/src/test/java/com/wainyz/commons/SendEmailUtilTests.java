package com.wainyz.commons;

import com.wainyz.commons.utils.SendEmailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 保证发送邮箱的功能正常
 */
@SpringBootTest
public class SendEmailUtilTests {

    @Autowired
    private SendEmailUtil sendEmailUtil;

    /**
     * 测试发送邮件功能
     */
    @Test
    public void testSendEmail() {
        sendEmailUtil.sendEmail("3175535553@qq.com","测试邮件","测试邮件内容");
    }
}
