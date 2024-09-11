package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Package:com.xuecheng.content.api
 * @Auther:Brianwei
 * @date:2024/6/24:14:26
 * @discribe:
 */
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private CoursePublisService coursePublisService;

    //根据课程id查询课程信息
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getProviewDto(@PathVariable("courseId") Long courseId){
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublisService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }
}
