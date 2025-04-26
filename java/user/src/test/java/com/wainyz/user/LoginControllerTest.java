package com.wainyz.user;

import com.wainyz.commons.pojo.domin.LoginDO;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.controller.LoginController;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.exception.LoginException;
import com.wainyz.user.validate.LoginCheck;
import jakarta.servlet.http.HttpServletResponse;
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
            LoginCheck.checkLoginParam(loginDO);
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            loginDO.setEmail(loginDO.getEmail().replace(" x", ""));
        }
        try {
            LoginCheck.checkLoginParam(loginDO);
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            loginDO.setPassword("12345678");
        }
        try {
            LoginCheck.checkLoginParam(loginDO);
            assert false;
        }catch (CheckException e){
            logger.info(e.getMessage());
            loginDO.setImageCode("1f68");
            loginDO.setImageId("185033");
        }
        try {
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
            loginController.login(loginDO, response);
            assert false;
        }catch (LoginException e){
            logger.info(e.getMessage());
            loginDO.setPassword(loginDO.getPassword().replace("x", ""));
        }catch (Exception e){}
        try{
            ReturnModel login = loginController.login(loginDO, response);
            logger.info(login.message);
        }catch (Exception e){
            logger.error(e.getMessage());
            assert false;
        }
    }

}
