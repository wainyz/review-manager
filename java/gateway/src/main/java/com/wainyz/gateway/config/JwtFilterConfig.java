package com.wainyz.gateway.config;

import com.wainyz.commons.utils.JwtUtils;
import com.wainyz.gateway.filters.JWTGlobalFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.*;

import static com.wainyz.commons.config.UrlConfiguration.PUBLIC_PREFIX;

/**
 * @author Yanion_gwgzh
 */
@Configuration
public class JwtFilterConfig {
    //----------------0------------------
    public final String PUBLIC_URL = PUBLIC_PREFIX ;
    public final String Filter_URL = "/*";
    @Autowired
    private JWTGlobalFilter jwtGlobalFilter;
    public JwtFilterConfig (
    ) {
    }
    //================0==================

    /**
     * 注册jwt过滤器
     * @return
     */
    @Bean
    public FilterRegistrationBean<JWTGlobalFilter> jwtGlobalFilterFilterRegistrationBean0() {
        FilterRegistrationBean<JWTGlobalFilter> registrationBean = new FilterRegistrationBean<>();
        // 注册 Filter
        registrationBean.setFilter(jwtGlobalFilter);
        // 设置拦截路径
        registrationBean.addUrlPatterns(Filter_URL);
        // 设置初始化参数
        registrationBean.addInitParameter("passUrl", PUBLIC_URL);
        // 增加日志确认参数传递
        System.out.println("Initializing JWTGlobalFilter with passUrl: " + PUBLIC_URL);
        // 设置优先级，先进行cors，然后才是jwt
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
