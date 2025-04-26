package com.wainyz.user.service;

import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.user.exception.LoginException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yanion_gwgzh
 */

public interface LoginValidateService {
    boolean validate(String token)throws LoginException ;
}
