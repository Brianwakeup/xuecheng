package com.xuecheng.content.feignClient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Package:com.xuecheng.content.feignClient
 * @Auther:Brianwei
 * @date:2024/9/5:8:31
 * @discribe: 使用FallbackFactory可以拿到熔断的异常信息
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

    //这里可以拿到熔断异常
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            //这里是处理熔断后的降级逻辑
            @Override
            public String upload(MultipartFile file, String objectName) {
                log.debug("出错：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
