package com.wainyz.user.exception;

/**
 * @author Yanion_gwgzh
 */
public class CheckException extends Exception {
    public final CheckReason reason;

    public CheckException(CheckReason reason) {
        super(reason.getMessage());
        this.reason = reason;
    }

    public CheckException(Exception e) {
        super(e);
        this.reason = CheckReason.UNKNOWN;
    }

    /**
     * 定义所有可能的移除状态码和消息，因为发现在test中，想要判断是否准确抛出某个移除非常的难
     * 可能抛出了异常，但实际上并不是我们预料中的，而我们却不知道，还以为程序正常运行。
     * 所以需要规范所有可能的异常，便于在test中判断是否抛出了预期的异常。
     */
    public enum CheckReason {
        /**
         * 未知异常类型
         */
        UNKNOWN("000", "未知异常"),
        /**
         * 邮箱格式不正确
         */
        EMAIL_FORMAT_ERROR("001", "邮箱格式不正确"),
        /**
         * 邮箱已被注册
         */
        EMAIL_HAS_REGISTERED("002", "邮箱已被注册"),
        /**
         * 邮箱未注册
         */
        EMAIL_NOT_REGISTERED("003", "邮箱未注册"),
        /**
         * 邮箱已被锁定
         */
        EMAIL_LOCKED("004", "邮箱已被锁定"),
        /**
         * 邮箱已被注销
         */
        EMAIL_CANCELLED("005", "邮箱已被注销"),
        /**
         * 邮箱验证码错误
         */
        EMAIL_CODE_ERROR("006", "邮箱验证码错误"),
        /**
         * 邮箱验证码已过期
         */
        EMAIL_CODE_EXPIRED("007", "邮箱验证码已过期"),
        /**
         * 邮箱验证码已存在，请勿重复发送
         */
        EMAIL_CODE_EXIST("008", "邮箱验证码已存在，请勿重复发送"),

        /**
         * 用户输入的密码与系统存储的密码不匹配
         * 常见于登录或身份验证场景
         * 建议用户检查密码是否正确或尝试找回密码
         */
        PASSWORD_ERROR("101", "密码错误"),

        /**
         * 密码不符合预定义格式规则
         * 通常包含未满足的格式要求（如缺少特殊字符/数字）
         * 建议用户参考密码规则说明修改密码
         */
        PASSWORD_FORMAT_ERROR("102", "密码格式不正确"),

        /**
         * 两次输入的密码内容不一致
         * 常见于注册/修改密码时的密码确认环节
         * 建议用户重新一致地输入两次密码
         */
        PASSWORD_NOT_MATCH("103", "两次密码不一致"),

        /**
         * 密码长度未达到最小要求
         * 具体最小长度限制根据系统策略定义
         * 建议用户增加密码字符长度
         */
        PASSWORD_TOO_SHORT("104", "密码太短"),

        /**
         * 密码长度超过最大允许限制
         * 具体最大长度限制根据系统策略定义
         * 建议用户缩短密码字符长度
         */
        PASSWORD_TOO_LONG("105", "密码太长"),

        /**
         * 密码包含空格字符
         * 空格符被视为无效密码字符
         * 建议用户移除密码中的空格字符
         */
        PASSWORD_CONTAIN_SPACE("106", "密码不能包含空格"),

        /**
         * 用户名已存在
         */
        USERNAME_EXISTS("201", "用户名已存在"),

        /**
         * 用户名格式不正确
         */
        USERNAME_FORMAT_ERROR("202", "用户名格式不正确"),

        /**
         * 用户名长度超出限制
         */
        USERNAME_TOO_LONG("203", "用户名太长"),

        /**
         * 用户名长度不足
         */
        USERNAME_TOO_SHORT("204", "用户名太短"),

        /**
         * 用户名包含非法字符
         */
        USERNAME_INVALID_CHARACTERS("205", "用户名包含非法字符"),

        /**
         * 用户未登录
         */
        USER_NOT_LOGGED_IN("301", "用户未登录"),

        /**
         * 用户会话已过期
         */
        SESSION_EXPIRED("302", "会话已过期"),

        /**
         * 用户权限不足
         */
        INSUFFICIENT_PERMISSIONS("303", "权限不足"),

        /**
         * 用户被锁定
         */
        USER_LOCKED("304", "用户被锁定"),

        /**
         * 用户被禁用
         */
        USER_DISABLED("305", "用户被禁用"),

        /**
         * 图片验证码错误
         */
        CAPTCHA_ERROR("401", "图片验证码错误"),

        /**
         * 图片验证码已过期
         */
        CAPTCHA_EXPIRED("402", "图片验证码已过期");

        private final String code;
        private final String message;

        CheckReason(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CheckException other) {
            return this.reason == other.reason;
        }
        return false;
    }
}