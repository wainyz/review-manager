package com.wainyz.gateway.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author Yanion_gwgzh
 */
@Configuration
public class CosWebMvcConfigurer implements WebMvcConfigurer {
    //----------0----------
    Logger logger = LoggerFactory.getLogger(CosWebMvcConfigurer.class);
    public final String mappingUrl;
    public final String allowedOrigins;
    public final String allowedMethods;
    public final String allowedHeaders;
    public CosWebMvcConfigurer(
            @Value("${cors.mappingUrl:/**}") String mappingUrl,
            @Value("${cors.allowedOrigins:http://localhost:5173}") String allowedOrigins,
            @Value("${cors.allowedMethods:*}") String allowedMethods,
            @Value("${cors.allowedHeaders:*}") String allowedHeaders
    ) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.mappingUrl = mappingUrl;
        logger.info("[configuration reading]cors config: mappingUrl:{},allowedOrigins:{},allowedMethods:{},allowedHeaders:{}",
                this.mappingUrl,
                this.allowedOrigins,
                this.allowedMethods,
                this.allowedHeaders);
    }
    //===========0=========

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许跨域访问的路径
        registry.addMapping(mappingUrl)
                // 允许的源（域名），可以是多个
                .allowedOrigins(allowedOrigins.split(","))
                // 允许的 HTTP 方法
                .allowedMethods(allowedMethods.split(","))
                // 允许的请求头
                .allowedHeaders(allowedHeaders.split(","))
                // 是否允许发送 Cookie
                .allowCredentials(true)
                // 预检请求的缓存时间（秒）
                .maxAge(3600);
    }

}
