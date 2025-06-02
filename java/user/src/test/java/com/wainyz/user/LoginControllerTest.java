package com.wainyz.user;

import com.wainyz.commons.pojo.domin.LoginDO;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.controller.LoginController;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.validate.LoginCheck;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class LoginControllerTest {
    Logger logger = org.slf4j.LoggerFactory.getLogger(LoginControllerTest.class);

    /**
     * 测试验证码等检验是否正常
     */
    @Test
    public void testLoginCheckParam() {
        LoginDO loginDO = new LoginDO();
        // 1 邮箱格式不对
        loginDO.setEmail("wainyz@163.com x");
        // 2 密码格式不对
        loginDO.setPassword("123456");
        // 3 验证码不对
        loginDO.setImageCode("1234");
        loginDO.setImageId("123456");
        try {
            logger.info("[system]开始测试：邮箱格式不对");
            LoginCheck.checkLoginParam(loginDO);
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            loginDO.setEmail(loginDO.getEmail().replace(" x", ""));
        }
        try {
            logger.info("[system]开始测试：密码格式不对");
            LoginCheck.checkLoginParam(loginDO);
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            loginDO.setPassword("12345678");
        }
        try {
            logger.info("[system]开始测试：邮箱验证码不对");
            LoginCheck.checkLoginParam(loginDO);
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            loginDO.setImageCode("1f68");
            loginDO.setImageId("185033");
        }
        try {
            logger.info("[system]开始测试：验证格式成功测试。");
            LoginCheck.checkLoginParam(loginDO);
            logger.info("验证成功");
        }catch (CheckException e){
            logger.error(e.getMessage());
            assert false;
        }
    }
    /**
     * 测试登录验证是否正常
     */
    @Autowired
    LoginController loginController;
    @Test
    public void testLoginController() {
        LoginDO loginDO = new LoginDO();
        loginDO.setEmail("player_simple@163.com");
        //错误密码
        loginDO.setPassword("***REMOVED***x");
        loginDO.setImageCode("1f68");
        loginDO.setImageId("185033");
        MockHttpServletResponse response = new MockHttpServletResponse();
        try{
            logger.info("[system]开始测试：密码错误触发");
            ReturnModel login = loginController.login(loginDO, response);
            assert login.getCode() != 200;
        }catch (Exception e){
            loginDO.setPassword("***REMOVED***");
        }
        try{
            logger.info("[system]开始测试：登录成功触发");
            ReturnModel login = loginController.login(loginDO, response);
            assert login.getCode() == 200;
            logger.info(login.message);
        }catch (Exception e){
            logger.error(e.getMessage());
            assert false;
        }
    }

    /**
     * 尝试使用username进行登录，这里的测试数据是 wainyz ***REMOVED***，需要确保这个数据存在于数据库中。
     */
    @Test
    public void testUsernameLogin(){
        LoginDO loginDO = new LoginDO();
        loginDO.setEmail("wainyz");
        //确保是 正确密码和正确的username
        loginDO.setPassword("***REMOVED***");
        loginDO.setImageCode("1f68");
        loginDO.setImageId("185033");
        logger.info("[system]开始测试：使用username正确登录到系统。");
        MockHttpServletResponse response = new MockHttpServletResponse();
        try{
            ReturnModel login = loginController.login(loginDO, response);
            assert login.getCode() == 200;
            logger.info(login.message);
        }catch (Exception e){
            logger.error(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 封禁登录测试,测试对象userId=2
     */
    @Test
    public void testBanUser(){
        logger.info("[system]开始测试：封禁对象登录触发。");
        LoginDO loginDO = new LoginDO();
        loginDO.setEmail("fuvhzc");
        //确保是 正确密码和正确的username
        loginDO.setPassword("***REMOVED***");
        loginDO.setImageCode("1f68");
        loginDO.setImageId("185033");
        MockHttpServletResponse response = new MockHttpServletResponse();
        try{
            ReturnModel login = loginController.login(loginDO, response);
            assert login.getCode() != 200;
            logger.info(login.message);
        }catch (Exception e){
            logger.error(e.toString());
            e.printStackTrace();
        }
    }

}
