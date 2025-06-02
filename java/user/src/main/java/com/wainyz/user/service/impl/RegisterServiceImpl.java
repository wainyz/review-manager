package com.wainyz.user.service.impl;

import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.commons.utils.RedisOps;
import com.wainyz.commons.utils.SendEmailUtil;
import com.wainyz.user.exception.CheckException;
import com.wainyz.user.pojo.domin.UserRegistryDO;
import com.wainyz.user.pojo.po.UserPO;
import com.wainyz.user.service.RegisterService;
import com.wainyz.user.service.UserService;
import com.wainyz.user.utils.MyBCryptPasswordEncoder;
import com.wainyz.user.utils.UsernameGenerator;
import com.wainyz.user.validate.LoginCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Yanion_gwgzh
 */
@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UsernameGenerator usernameGenerator;
    @Autowired
    private MyBCryptPasswordEncoder encoder;
    @Value("${user.register.email_code.timeout:60}")
    private int timeout;
    @Autowired
    SendEmailUtil sendEmailUtil;
    @Autowired
    RedisOps redisOps;
    @Autowired
    private UserService userService;

    /**
     * 用户注册函数。
     * <p>
     * 该函数用于处理用户注册逻辑，包括邮箱格式校验、验证码校验、密码格式校验、邮箱唯一性检查以及用户信息的存储。
     *
     * @param model 包含用户注册信息的数据对象，包括邮箱、密码和邮箱验证码等信息。
     *              - email: 用户输入的邮箱地址。
     *              - password: 用户输入的密码。
     *              - emailCode: 用户输入的邮箱验证码。
     * @return ReturnModel 注册结果的封装对象。
     * - 成功时返回成功消息 "注册成功"。
     * - 失败时返回具体的错误信息，例如 "邮箱格式不对"、"密码格式不对"、"邮箱已存在" 或 "验证码错误"。
     */
    @Override
    public ReturnModel registry(UserRegistryDO model) throws CheckException {
        // 从 Redis 中获取邮箱验证码并进行匹配校验
        String redisEmailCode = redisOps.get(RedisOps.RedisKeyPrefix.Register_Email,model.getEmail());

        if (redisEmailCode == null) {
            throw new CheckException(CheckException.CheckReason.EMAIL_CODE_EXPIRED);
        }

        if (!Objects.equals(redisEmailCode, model.getEmailCode())) {
            throw new CheckException(CheckException.CheckReason.EMAIL_CODE_ERROR);
        }

        // 校验密码长度是否在 8 到 16 个字符之间
        if (model.getPassword() == null || model.getPassword().isEmpty()) {
            throw new CheckException(CheckException.CheckReason.PASSWORD_TOO_SHORT);
        }

        if (model.getPassword().length() <= 7 || model.getPassword().length() >= 17) {
            throw new CheckException(CheckException.CheckReason.PASSWORD_FORMAT_ERROR);
        }

        // 检查邮箱是否已被注册
        try {
            if (userService.getUserByEmail(model.getEmail()) != null) {
                throw new CheckException(CheckException.CheckReason.EMAIL_HAS_REGISTERED);
            }
        } catch (RuntimeException e) {
            throw new CheckException(e);
        }

        // 调用用户服务进行注册操作，并根据结果返回成功或失败信息
        try {
            // 调用bcrypt生成加盐密码
            String password = encoder.encode(model.getPassword());
            UserPO userPO = new UserPO().setEmail(model.getEmail()).setPassword(password).setUsername(usernameGenerator.generateUsername(model.getEmail(),model.getEmailCode()));
            if (userService.register(userPO) != null) {
                redisOps.delete(RedisOps.RedisKeyPrefix.Register_Email,model.getEmail());
                return ReturnModel.success().setMessage("注册成功");
            } else {
                return ReturnModel.fail().setMessage("注册失败");
            }
        } catch (Exception e) {
            return ReturnModel.fail().setMessage("系统错误，请稍后再试");
        }
    }

    /**
     * 发送邮件验证码到指定邮箱地址
     *
     * @param email 接收验证码的邮箱地址，需符合标准邮箱格式
     * @return 包含操作结果的ReturnModel对象，包含成功/失败状态和提示信息
     */
    @Override
    public ReturnModel sendEmail(String email, SendReason reason) throws CheckException{
        // 邮箱格式校验（使用预定义的正则表达式）
        if (!email.matches(LoginCheck.emailPatternString)) {
            throw new CheckException(CheckException.CheckReason.EMAIL_FORMAT_ERROR);
        }
        // 邮箱重复校验（数据库中已存在该邮箱地址）
        if (reason == SendReason.Registry && userService.getUserByEmail(email) != null) {
            throw new CheckException(CheckException.CheckReason.EMAIL_HAS_REGISTERED);
        }
        if (reason == SendReason.Login && userService.getUserByEmail(email) == null) {
            throw new CheckException(CheckException.CheckReason.EMAIL_NOT_REGISTERED);
        }
        if (reason == SendReason.ChangePassword && userService.getUserByEmail(email) == null) {
            throw new CheckException(CheckException.CheckReason.EMAIL_NOT_REGISTERED);
        }

        // 防刷机制：检查Redis中是否已存在未过期的验证码
        if (redisOps.get(RedisOps.RedisKeyPrefix.Register_Email,email) != null) {
            throw new CheckException(CheckException.CheckReason.EMAIL_CODE_EXIST);
        }

        // 生成并发送验证码邮件
        String code = sendEmailUtil.sendEmail(email);

        // 存储验证码到Redis并设置超时（TIMEOUT单位：秒）
        redisOps.setAndExpire(RedisOps.RedisKeyPrefix.Register_Email, email, code, timeout, TimeUnit.SECONDS);

        return ReturnModel.success().setMessage("邮件发送成功，" + timeout + "s内有效");
    }

    @Override
    public boolean deleteUser(String id) {
        userService.removeById(id);
        return true;
    }

    /**
     * 枚举类 SendReason 定义了发送操作的原因。
     * 该枚举用于标识特定的操作场景，例如注册、修改密码或登录。
     * <p>
     * 枚举值说明：
     * - Registry: 表示用户注册操作。
     * - ChangePassword: 表示用户修改密码操作。
     * - Login: 表示用户登录操作。
     */
    public enum SendReason {
        Registry, // 用户注册操作
        ChangePassword, // 用户修改密码操作
        Login // 用户登录操作
    }
}
