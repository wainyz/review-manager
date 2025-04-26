package com.wainyz.user.aop;

import com.wainyz.commons.pojo.domin.LoginDO;
import com.wainyz.commons.pojo.vo.ReturnModel;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.jdbc.core.metadata.GenericTableMetaDataProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import static com.wainyz.user.controller.LoginController.logger;
/**
 * 实现日志
 * @author Yanion_gwgzh
 */
@Aspect
@Component
public class LoginLoggerAspect {
    /**
     * jwt 过滤器日志,主要用于显示token失败的原因
     */

    @Pointcut(value = "execution(* com.wainyz.user.controller.LoginController.login(..))")
    void jwtGlobalFilterPointCut(){
    }
    @Around(value = "jwtGlobalFilterPointCut()")
    public ReturnModel jwtGlobalFilterLogger(ProceedingJoinPoint joinPoint) throws Throwable {
        LoginDO loginDo = (LoginDO) joinPoint.getArgs()[0];
        Long id = Instant.now().getEpochSecond() & 0b111111;
        logger.info("[Login starting {}]: url {}", id ,loginDo);
        try {
            ReturnModel returnModel = (ReturnModel) joinPoint.proceed();
            logger.info("[Login ending {}]: result {}", id , returnModel);
            return returnModel;
        }catch (Exception e){
            logger.info("[Login ending {}]: error {}", id , e.getMessage());
            throw e;
        }
    }
}
