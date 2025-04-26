package com.wainyz.user.validate;
import com.wainyz.commons.pojo.domin.LoginDO;
import com.wainyz.user.exception.CheckException;

/**
 * @author Yanion_gwgzh
 */
public class LoginCheck {
    public static String emailPatternString  = ".+?@.+?[.]\\w+";
    public static void checkLoginParam(LoginDO loginDO) throws CheckException {
        // email check
        String email = loginDO.getEmail();
        if(!email.matches(emailPatternString)){
            throw new CheckException(CheckException.CheckReason.EMAIL_FORMAT_ERROR);
        }
        // check password
        if(loginDO.getPassword().length() < 8 || loginDO.getPassword().length()>16  ){
            throw new CheckException(CheckException.CheckReason.PASSWORD_FORMAT_ERROR);
        }
        // check image code
        if(!ImageCodeCheck.checkImageCode(loginDO.getImageId(), loginDO.getImageCode())){
            throw new CheckException(CheckException.CheckReason.CAPTCHA_ERROR);
        }
    }
}
