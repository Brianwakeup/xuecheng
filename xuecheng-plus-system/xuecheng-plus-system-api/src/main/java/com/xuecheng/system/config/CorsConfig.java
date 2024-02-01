package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @Package:com.xuecheng.base.config
 * @Auther:Brianwei
 * @date:2024/1/29:15:05
 * @discribe:
 */

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        //添加cors配置信息
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许跨这个地址的域，不要写*，否则cookie无法使用
        corsConfiguration.addAllowedOrigin("http://localhost:8601");
        //是否发送cookie信息
        corsConfiguration.setAllowCredentials(true);
        //请求方法
        corsConfiguration.addAllowedMethod("*");
        //允许的头信息,允许所有请求头进来
        corsConfiguration.addAllowedHeader("*");

        //添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);

        //返回新的corsfilter
        return new CorsFilter(source);
    }
}
