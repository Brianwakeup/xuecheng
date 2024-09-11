package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Package:com.xuecheng
 * @Auther:Brianwei
 * @date:2024/1/26:17:52
 * @discribe:
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignClient"})
@EnableSwagger2Doc
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
