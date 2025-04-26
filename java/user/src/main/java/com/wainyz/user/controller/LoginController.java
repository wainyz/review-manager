package com.wainyz.user.controller;

import com.wainyz.commons.pojo.domin.LoginDO;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.commons.pojo.vo.UserVO;
import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.exception.LoginException;
import com.wainyz.user.service.UserService;
import com.wainyz.user.validate.LoginCheck;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import static com.wainyz.commons.consistent.GatewayConsistent.TOKEN_HEADER;

/**
 * @author Yanion_gwgzh
 */
@RestController
public class LoginController {
    public static final String LOGIN_URL = "/public/login";
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtTokenUtil;
    private final String defaultUsername = "username";
    public static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @PostMapping(LOGIN_URL)
    public ReturnModel login(@RequestBody LoginDO loginDO , HttpServletResponse response) throws CheckException, LoginException {
        //  检查参数格式和图片验证码
        LoginCheck.checkLoginParam(loginDO);

        UserVO login = userService.login(loginDO.getEmail(), loginDO.getPassword());
        if(login!=null && login.getEmail()!=null){
            ReturnModel returnModel = ReturnModel.success();
            returnModel.setMessage("登录成功");
            returnModel.setData(login);
            // 设置token
            Map<String,Object> map = new HashMap<>(2);
            map.put(GatewayConsistent.USER_ID,login.getUserId().toString());
            map.put(GatewayConsistent.USER_EMAIL, login.getEmail());
            //存入jwt
            String token = jwtTokenUtil.generateToken(new DefaultClaims(map));
            response.setHeader(TOKEN_HEADER, token);
            login.setAuthorization(token);
            response.setStatus(HttpServletResponse.SC_OK);
            return returnModel;
        }else{
            throw new LoginException("账号密码错误");
        }
    }
}
