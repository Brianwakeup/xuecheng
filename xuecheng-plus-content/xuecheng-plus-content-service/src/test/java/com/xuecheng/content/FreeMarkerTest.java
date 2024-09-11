package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublisService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @Package:com.xuecheng.content
 * @Auther:Brianwei
 * @date:2024/8/28:17:09
 * @discribe: 测试freemarker
 */
@SpringBootTest
public class FreeMarkerTest {

    @Autowired
    CoursePublisService coursePublisService;

    @Test
    public void test1() throws IOException, TemplateException, URISyntaxException {
        Configuration configuration = new Configuration(Configuration.getVersion());
        //拿到classpath路径
        String classpath = Paths.get(this.getClass().getResource("/templates").toURI()).toString();
        configuration.setDirectoryForTemplateLoading(new File(classpath));
        //指定编码
        configuration.setDefaultEncoding("utf-8");
        //得到模板
        Template template = configuration.getTemplate("course_template.ftl");
        //得到数据
        CoursePreviewDto coursePreviewInfo = coursePublisService.getCoursePreviewInfo(120L);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("model",coursePreviewInfo);
        String s = FreeMarkerTemplateUtils.processTemplateIntoString(template, hashMap);
        //读取
        InputStream inputStream = IOUtils.toInputStream(s, "utf-8");
        //写入
        Long id = coursePreviewInfo.getCourseBase().getId();
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\" + id + ".html");
        IOUtils.copy(inputStream,fileOutputStream);
    }
}
