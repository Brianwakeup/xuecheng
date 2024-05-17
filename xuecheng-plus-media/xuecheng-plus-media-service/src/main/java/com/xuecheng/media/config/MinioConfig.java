package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Package:com.xuecheng.media.config
 * @Auther:Brianwei
 * @date:2024/2/26:12:34
 * @discribe:
 */
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    MinioClient client(){
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey,secretKey)
                .build();
    }
}
