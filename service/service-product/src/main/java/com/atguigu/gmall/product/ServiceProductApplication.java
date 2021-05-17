package com.atguigu.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan({"com.atguigu.gmall"})
//@EnableDiscoveryClient
//@EnableFeignClients(basePackages = "com.atguigu.gmall")
//@EnableSwagger2
public class ServiceProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class,args);
    }
}
