package com.wainyz.commons;

import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.exception.MyJwtException;
import com.wainyz.commons.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JwtUtilsTests {
    @Autowired
    JwtUtils jwtUtils;
    Logger logger = LoggerFactory.getLogger(JwtUtilsTests.class);

    private static Map<String,String> testData;

    /**
     * 构建测试数据
     */
    @BeforeAll
    public static void init()  {
        testData = new HashMap<>();
        testData.put(GatewayConsistent.USER_ID,"1");
        testData.put(GatewayConsistent.USER_EMAIL,"wainyz@qq.com");
    }

    /**
     * 测试生成token功能
     */
    @Test
    public void testGenerateToken() {
        logger.info("[generate test] begin:");
        String token = jwtUtils.generateToken(new DefaultClaims(testData),1000000000000000L);
        assert token != null;
        logger.info("[generate test] end token:\n{}",token);
    }
    /**
     * 测试解析token功能
     */
    @Test
    public void testParseToken() {
        logger.info("[parse test] begin:");
        String token = jwtUtils.generateToken(new DefaultClaims(testData));
        assert token != null;
        try {
            Claims claims = jwtUtils.parseToken(token);
            for (Map.Entry<String,String> entry : testData.entrySet())
            {
                assert entry.getValue().equals(claims.get(entry.getKey()));
            }
            logger.info("[parse test] end token:\n{}", claims.toString());
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
    /**
     * 测试token失败，包括：
     * 1. token为空
     * 2. token错误
     * 3. token过期
     * 4. token被篡改
     */
    @Test
    public void testValidateToken() throws InterruptedException {
        logger.info("[validate test] begin:");
        String token = jwtUtils.generateToken(new DefaultClaims(testData));
        Map<String,String> badData = new HashMap<>();
        //使用错误的数据模拟生成篡改的token
        badData.put(GatewayConsistent.USER_ID,"2");
        badData.put(GatewayConsistent.USER_EMAIL,"wainyz@qq.com");
        String badToken = jwtUtils.generateToken(new DefaultClaims(badData));
        badToken = badToken.substring(0, badToken.lastIndexOf(".")) +
                token.substring(token.lastIndexOf("."));
        //生成过期的token,负数会无法解析
        String expiredToken = jwtUtils.generateToken(new DefaultClaims(testData),1);
        Thread.sleep(1000);
        try {
            jwtUtils.validate("");
            assert false;
        }catch (MyJwtException e){
            logger.info(e.getMessage());
            assert e.getExceptionReason() == MyJwtException.MyJwtExceptionEnum.INVALID_TOKEN;
        }
        try {
            jwtUtils.validate("123sopaijfioajsf");
            assert false;
        }catch (MyJwtException e){
            logger.info(e.getMessage());
            assert e.getExceptionReason() == MyJwtException.MyJwtExceptionEnum.OTHER_ERROR;
        }
        try {
            jwtUtils.validate(expiredToken);
            assert false;
        }catch (MyJwtException e){
            logger.info(e.getMessage());
            assert e.getExceptionReason() == MyJwtException.MyJwtExceptionEnum.EXPIRED_TOKEN;
        }
        try {
            jwtUtils.validate(badToken);
            assert false;
        }catch (MyJwtException e){
            logger.info(e.getMessage());
            assert e.getExceptionReason() == MyJwtException.MyJwtExceptionEnum.OTHER_ERROR;
        }
        logger.info("[validate test] end");
    }

    /**
     * 测试之前的token能否解析
     */
    @Test
    public void oldTokenTest() throws MyJwtException {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiIxIiwiZW1haWwiOiJwbGF5ZXJfc2ltcGxlQDE2My5jb20iLCJpYXQiOjE3NDcxOTExNzEsImV4cCI6MTc0NzI3NzU3MX0.uoiEboGs98kvbqleABmCH7kkSHI7IhjpSro-j2MoX5rv9EndHm7ITDss4w62eSt0mqDqCgQMPfXIlWc3AwRzQQ";
        Claims claims = jwtUtils.parseToken(token);
        System.out.println(claims);
        assert !claims.isEmpty();
    }
}
