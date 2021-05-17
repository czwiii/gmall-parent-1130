package com.atguigu.gmall.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.atguigu.gmall"})
public class ServiceTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceTestApplication.class,args);
    }
}
