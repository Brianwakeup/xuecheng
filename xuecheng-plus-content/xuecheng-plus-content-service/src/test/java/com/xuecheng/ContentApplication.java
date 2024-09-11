package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Package:com.xuecheng
 * @Auther:Brianwei
 * @date:2024/1/26:17:52
 * @discribe:
 */
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignClient"})
@SpringBootApplication
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
