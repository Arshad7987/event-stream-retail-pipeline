package com.arshadansari.retailpipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RetailPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailPipelineApplication.class, args);
    }
}
