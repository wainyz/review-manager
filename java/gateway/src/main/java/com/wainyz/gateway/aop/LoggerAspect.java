package com.wainyz.gateway.aop;

import com.wainyz.gateway.filters.JWTGlobalFilter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.wainyz.gateway.filters.JWTGlobalFilter.LOGGER;

/**
 * 实现日志
 * @author Yanion_gwgzh
 */
@Aspect
@Component
public class LoggerAspect {
    /**
     * jwt 过滤器日志,主要用于显示token失败的原因
     */

    @Pointcut("execution(void com.wainyz.gateway.filters.JWTGlobalFilter.doFilter(..))")
    void jwtGlobalFilterPointCut(){}
    @Around("jwtGlobalFilterPointCut()")
    public void jwtGlobalFilterLogger(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest servletRequest = (HttpServletRequest) joinPoint.getArgs()[0];
        HttpServletResponse servletResponse = (HttpServletResponse) joinPoint.getArgs()[1];
        Long id = Instant.now().getEpochSecond() & 0b111111;
        LOGGER.info("[GlobalFilter doFilter {}]: url {}", id ,servletRequest.getRequestURL());
        joinPoint.proceed();
        LOGGER.info("[GlobalFilter endFilter {}]: status {}", id ,servletResponse.getStatus());
    }
}
