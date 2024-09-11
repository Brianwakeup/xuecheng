package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Package:com.xuecheng.content.api
 * @Auther:Brianwei
 * @date:2024/6/19:21:39
 * @discribe:
 */
@Controller
public class CoursePubliseController {

    @Autowired
    CoursePublisService coursePublisService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewInfo = coursePublisService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model",coursePreviewInfo);
        System.out.println(coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        coursePublisService.commitAudit(1L,courseId);
    }

    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        coursePublisService.coursepublish(1L,courseId);
    }
}
