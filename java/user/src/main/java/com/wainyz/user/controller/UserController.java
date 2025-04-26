package com.wainyz.user.controller;


import com.wainyz.commons.pojo.domin.UserDO;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.convert.UserConvert;
import com.wainyz.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("user")
public class UserController {
    @GetMapping("/")
    public ReturnModel ping(){
        return ReturnModel.success();
    }
    @Autowired
    private UserService userService;
    /**
     * 公开接口查询用户基本信息
     * @param userId
     * @return
     */
    @PostMapping("/public/{user_id}")
    ReturnModel getUser(@PathVariable("user_id") Long userId){
        // 验证是否是本人
        UserDO user = userService.getUserById(userId);
        return ReturnModel.success().setData(UserConvert.INSTANCE.doTovo(user));
    }

    /**
     * 登录用户改密码前的发送
     * @return
     */
    private String emailCode;
    @PostMapping("/change/password/send")
    ReturnModel changePasswordSendEmail(@RequestAttribute("user_id")Long userId){
        //TODO 获取email

        // 将code放在mq中，如果时间到了自动删除
        return null;
    }
}
