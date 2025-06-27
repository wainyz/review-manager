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
            @Value("${cors.allowedOrigins:http://localhost:5173,https://wainyz.online}") String allowedOrigins,
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
        registry.addMapping("/**")
                .allowedOrigins(
                        allowedOrigins.split(",")
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Headers",
                        "Access-Control-Allow-Methods",
                        "Access-Control-Allow-Origin",
                        "Access-Control-Max-Age",
                        "Access-Control-Request-Headers",
                        "X-Frame-Options")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
