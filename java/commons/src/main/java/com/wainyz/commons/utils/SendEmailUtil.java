package com.wainyz.commons.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author Yanion_gwgzh
 */
@Component
public class SendEmailUtil {
    private final JavaMailSender mailSender;
    private final String sourceEmail;
    private static final String REGISTER_SUBJECT = "注册验证码";
    public SendEmailUtil (@Autowired JavaMailSender mailSender,@Value("${spring.mail.username}") String sourceEmail) {
        this.mailSender = mailSender;
        this.sourceEmail = sourceEmail;
    }
    public String sendEmail(String email) {
        String code = String.valueOf((new Random().nextInt(1000, 9999)));
        return sendEmail(email, REGISTER_SUBJECT, code);
    }
    public String sendEmail(String email, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sourceEmail);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
        return content;
    }
}
