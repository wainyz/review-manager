package com.wainyz.user.service.impl;

import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.user.exception.LoginException;
import com.wainyz.user.service.LoginValidateService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yanion_gwgzh
 */
@Service
public class JwtLoginValidateServiceImpl implements LoginValidateService {
    @Autowired
    private JwtUtils jwtUtils;
    @Override
    public boolean validate(String token) throws LoginException {
        return false;
    }
}
