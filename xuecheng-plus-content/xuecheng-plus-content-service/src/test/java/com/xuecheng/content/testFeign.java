package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Package:com.xuecheng.content
 * @Auther:Brianwei
 * @date:2024/9/3:15:13
 * @discribe:
 */
@SpringBootTest
public class testFeign {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test1(){
        File file = new File("D:\\120.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        mediaServiceClient.upload(multipartFile,"course/120.html");
    }
}
