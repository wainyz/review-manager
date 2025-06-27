package com.wainyz.commons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Yanion_gwgzh
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.wainyz"})
public class CommonsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommonsApplication.class, args);
    }
}
