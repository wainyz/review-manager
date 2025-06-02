package com.wainyz.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wainyz.commons.exception.MyJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * 用于 生成token以及解析token
 *
 * @author Yanion_gwgzh
 */
@Component
public class JwtUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);
    private final String secretKeyString;
    private final long expirationTime;
    private final SecretKey secretKey;
    //----------0------------
    @Autowired
    public ObjectMapper objectMapper;

    public JwtUtils(
            @Value("${jwt.secretKey:eyJ1c2VySWQiOiIxIiwiZW1haWwiOiJ3YWlueXpAcXEuY29tIiwiaWF0IjoxNzQ3MTkwMDQ3LCJleHAiOjE3NDcyNzY0NDd9eyJ1c2VySWQiOiIxIiwiZW1haWwiOiJ3YWlueXpAcXEuY29tIiwiaWF0IjoxNzQ3MTkwMDQ3LCJleHAiOjE3NDcyNzY0NDd9eyJ1c2VySWQiOiIxIiwiZW1haWwiOiJ3YWlueXpAcXEuY29tIiwiaWF0Ijoasdf3}") String secretKeyString,
            @Value("${jwt.expirationTime:86400}") String expirationTime
    ) {
        this.secretKeyString = expendSecretKey(secretKeyString, 256);
        this.expirationTime = Long.parseLong(expirationTime);
//        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        //this.secretKey = Jwts.SIG.HS256.key().build();
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 防止秘钥不够长
     *
     * @param shortKey
     * @param wantLength
     * @return
     */
    public String expendSecretKey(String shortKey, int wantLength) {
        StringBuilder builder = new StringBuilder(shortKey);
        while (builder.length() < wantLength) {
            builder.append(UUID.randomUUID());
        }
        return builder.toString();
    }
    //=============0===============
    //-----------1-------------

    /**
     * 核心方法
     *
     * @return
     */
    public String generateToken(
            Claims claims,
            long expireTime
    ) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expireTime)))
                .signWith(secretKey);
        return jwtBuilder.compact();
    }

    public String generateToken(Claims claims) {
        return generateToken(claims, expirationTime);
    }

    public Claims validate(String token) throws MyJwtException {
        Claims claims = parseToken(token);
        expiration(claims);
        return claims;
    }

    public boolean expiration(Claims claims) throws MyJwtException {
        try {
            return claims.getExpiration().before(Date.from(Instant.now()));
        } catch (Exception e) {
            throw new MyJwtException(MyJwtException.MyJwtExceptionEnum.EXPIRED_TOKEN,"[89]token已过期");
        }
    }

    public Claims parseToken(String token) throws MyJwtException {
        if (token == null || token.isEmpty()) {
            throw new MyJwtException(MyJwtException.MyJwtExceptionEnum.INVALID_TOKEN,"[94]token为空");
        }
        try {
            Claims payload = Jwts.parser().verifyWith(secretKey)
                    .build()
                    .parse(token)
                    .accept(Jws.CLAIMS)
                    .getPayload();
            if (payload == null) {
                throw new MyJwtException(MyJwtException.MyJwtExceptionEnum.INVALID_TOKEN,"[102]token无效");
            } else {
                return payload;
            }
        } catch (ExpiredJwtException e) {
            throw new MyJwtException(MyJwtException.MyJwtExceptionEnum.EXPIRED_TOKEN,"[105]token已过期");
        } catch (RuntimeException e) {
            throw new MyJwtException(MyJwtException.MyJwtExceptionEnum.OTHER_ERROR , e.getMessage());
        }
    }
}
