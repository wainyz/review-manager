package com.wainyz.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Yanion_gwgzh
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.wainyz"})
public class CoreApplication {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

}
