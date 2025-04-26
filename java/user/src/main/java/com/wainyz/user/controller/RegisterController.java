package com.wainyz.user.controller;

import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.pojo.domin.UserRegistryDO;
import com.wainyz.user.pojo.po.SendEmailRequestData;
import com.wainyz.user.service.RegisterService;
import com.wainyz.user.service.impl.RegisterServiceImpl;
import com.wainyz.user.validate.ImageCodeCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.wainyz.commons.config.UrlConfiguration.PUBLIC_PREFIX;
import static com.wainyz.user.controller.RegisterController.REGISTER_URL;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping(PUBLIC_PREFIX)
public class RegisterController {
    public static final String REGISTER_URL = "/register/";
    @Autowired
    RegisterService registerService;

    /**
     * model的数据架构如下
     *     String email;
     *     String emailCode;
     *     String password;
     */
    @PostMapping("/register")
    public ReturnModel register(@RequestBody UserRegistryDO model) throws CheckException {
        // 调用注册服务处理用户注册逻辑
        return registerService.registry(model);
    }
    @DeleteMapping("/register")
    public ReturnModel delete(Model model) {
        if (registerService.deleteUser(String.valueOf(model.getAttribute(GatewayConsistent.USER_ID)))) {
            return ReturnModel.success().setMessage("删除成功");
        } else {
            return ReturnModel.fail().setMessage("删除失败");
        }
    }


    /**
     需要发送image_id和image_code，
     当正确，会发送邮箱，并在redis中记录一个60s的email锁。
     data的数据架构如下：
     public String email;
     public String imageId;
     public String imageCode;
     */
    @PostMapping("/register/send/email")
    public ReturnModel sendEmail(@RequestBody SendEmailRequestData data) throws CheckException {
        if(!ImageCodeCheck.checkImageCode(data.imageId, data.imageCode)){
            return ReturnModel.fail().setMessage("图片验证码错误");
        }
        return registerService.sendEmail(data.email, RegisterServiceImpl.SendReason.Registry);
    }

    /**
     * 发送忘记密码相关的邮件。
     *
     * @param email 用户的电子邮件地址，通过请求参数传递。
     * @return 返回一个 ReturnModel 对象，包含发送邮件的结果信息。
     * <p>
     * 该方法通过调用 registerService 的 sendEmail 方法，
     * 使用指定的邮箱地址和忘记密码原因（ChangePassword）发送邮件。
     */
    @PostMapping("/register/send/forgetPassword")
    ReturnModel forgetPassword(@RequestParam("email") String email) throws CheckException {
        return registerService.sendEmail(email, RegisterServiceImpl.SendReason.ChangePassword);
    }

}