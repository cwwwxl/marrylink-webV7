package com.marrylink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.marrylink.mapper")
@EnableScheduling
public class MarrylinkAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarrylinkAdminApplication.class, args);
    }
}