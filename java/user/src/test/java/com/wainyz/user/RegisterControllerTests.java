package com.wainyz.user;

import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.commons.utils.RedisOps;
import com.wainyz.user.controller.RegisterController;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.pojo.domin.UserRegistryDO;
import com.wainyz.user.pojo.po.SendEmailRequestData;
import com.wainyz.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

/**
 * 测试注册功能以及附带组件是否正常
 */
@SpringBootTest
@ActiveProfiles("test")
public class RegisterControllerTests {
    Logger logger = org.slf4j.LoggerFactory.getLogger(RegisterControllerTests.class);
    @Autowired
    RedisOps redisOps;
    /**
     * 测试controller的sendEmail方法
     */
    @Autowired
    RegisterController registerController;
    HttpServletResponse response = new MockHttpServletResponse();
    @Test
    public void testSendEmail() {
        SendEmailRequestData sendEmailRequestData = new SendEmailRequestData();
        // 已存在的邮箱
        logger.info("[system] 开始测试：存在邮箱异常触发。");
        sendEmailRequestData.email = "player_simple@163.com";
        sendEmailRequestData.setImageCode("1f68");
        sendEmailRequestData.setImageId("185033");
        try {
            ReturnModel returnModel = registerController.sendEmail(sendEmailRequestData);
            logger.error("未能抛出 邮箱已存在异常.");
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            assert CheckException.CheckReason.EMAIL_HAS_REGISTERED.equals(e.reason);
        }finally {
            redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,sendEmailRequestData.getEmail());
        }
        // 正常发送邮件数据
        logger.info("[system]开始测试: 未注册的正常邮箱流程。");
        sendEmailRequestData.setEmail("3175535553@qq.com");
        try {
            ReturnModel returnModel = registerController.sendEmail(sendEmailRequestData);
            if (returnModel.getCode() == HttpServletResponse.SC_OK){
                logger.info(returnModel.getMessage());
                assert true;
            }else {
                //注意必须保证测试邮箱没有注册过
                logger.error(returnModel.getMessage());
                assert false;
            }
        }catch (Exception e){
            logger.info(e.getMessage());
            assert false;
        }finally {
            // destroy 删除redis中的记录，防止影响其他测试
            redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,sendEmailRequestData.getEmail());
        }
    }
    /**
     * 测试注册服务,注册服务是检测redis中是否存在来判断是否发送验证码的，所以需要前提send email方法
     */
    @Autowired
    UserService userService;
    @Test
    public void testRegister(){
        UserRegistryDO userRegistryDO = new UserRegistryDO();
        // 没有发送邮箱验证码，或者验证码已经过期的情况
        logger.info("[system]开始测试：邮箱验证码校验异常触发。");
        userRegistryDO.setEmail("3175535553@qq.com");
        userRegistryDO.setEmailCode("123456");
        try {
            registerController.register(userRegistryDO);
            assert false;
        } catch (CheckException e) {
            logger.info("没有发送邮箱验证码，或者验证码已经过期的情况 =>{}",e.getMessage());
            assert CheckException.CheckReason.EMAIL_CODE_EXPIRED.equals(e.reason);
        }
        // 发送邮箱验证码，但是验证码错误
        try {
            redisOps.setAndExpire(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail(),"1asfa1",60,TimeUnit.SECONDS);
            registerController.register(userRegistryDO);
            assert false;
        } catch (CheckException e) {
            logger.info("验证码错误 =>{}",e.getMessage());
            assert CheckException.CheckReason.EMAIL_CODE_ERROR.equals(e.reason);
        }finally {
            redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail());
        }
        // 发送邮箱验证码，并且验证码正确(此时应该触发密码不能为空)
        try {
            redisOps.setAndExpire(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail(),"123456",60,TimeUnit.SECONDS);
            userRegistryDO.setEmailCode("123456");
            ReturnModel register = registerController.register(userRegistryDO);
            logger.error(register.message);
            assert false;
        } catch (CheckException e) {
            logger.info("应该触发密码不能为空 =>{}",e.getMessage());
            assert CheckException.CheckReason.PASSWORD_TOO_SHORT.equals(e.reason);
        }finally {
            redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail());
        }

        // 重新设置了密码，并改成了已经注册的邮箱账户。此时应该触发用户已注册（必须保证测试邮箱注册过)
        try {
            logger.info("[system]开始测试: 邮箱已经注册");
            userRegistryDO.setPassword("1234512312316");
            userRegistryDO.setEmail("player_simple@163.com");
            redisOps.setAndExpire(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail(),"123456",60,TimeUnit.SECONDS);
            userRegistryDO.setEmailCode("123456");
            //注册过的邮箱
            ReturnModel register = registerController.register(userRegistryDO);
            logger.error(register.message);
            assert false;
        } catch (CheckException e) {
            //可能的错误原因是，user service未能检查到邮箱是否已经注册过
            logger.info("应该触发用户已注册 =>{}",e.getMessage());
            assert CheckException.CheckReason.EMAIL_HAS_REGISTERED.equals(e.reason);
        }finally {
            redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail());
        }

        //最后注册成功的测试
        try {
            logger.info("[system]开始测试：邮箱正常注册成功。");
            userRegistryDO.setEmail("3175535553@163.com");
            userRegistryDO.setPassword("1234512312316");
            userService.deleteUser(userRegistryDO.getEmail());
            redisOps.setAndExpire(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail(),"123456",60,TimeUnit.SECONDS);
            userRegistryDO.setEmailCode("123456");
            //注册过的邮箱
            ReturnModel register = registerController.register(userRegistryDO);
            logger.info("应该触发 注册成功 =>{}", register.message);
            assert true;
        } catch (CheckException e) {
            logger.error(e.getMessage());
        }finally {
            redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,userRegistryDO.getEmail());
            userService.deleteUser(userRegistryDO.getEmail());
        }
    }
}
