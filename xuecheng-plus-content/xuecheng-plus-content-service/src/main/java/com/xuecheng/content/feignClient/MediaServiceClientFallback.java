package com.xuecheng.content.feignClient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Package:com.xuecheng.content.feignClient
 * @Auther:Brianwei
 * @date:2024/9/5:8:20
 * @discribe: 使用fallback定义降级的类，但是获取不到熔断的异常
 */
public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String upload(MultipartFile file, String objectName) {
        System.out.println("fallback");
        return null;
    }
}
