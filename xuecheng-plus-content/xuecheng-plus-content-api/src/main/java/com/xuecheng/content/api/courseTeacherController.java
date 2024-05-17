package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Package:com.xuecheng.content.api
 * @Auther:Brianwei
 * @date:2024/2/14:15:17
 * @discribe:
 */
@RestController
@Api(value = "课程教师编辑接口",tags = "课程教师编辑接口")
@Slf4j
@RequestMapping("/courseTeacher")
public class courseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @GetMapping("/list/{id}")
    @ApiOperation("查询课程对应教师")
    public CourseTeacher list(@PathVariable("id") Long id){
        log.info("查询{}号教师",id);
        CourseTeacher courseTeacher = courseTeacherService.list(id);
        return courseTeacher;
    }

    @PostMapping
    @ApiOperation("添加教师")
    public CourseTeacher addCourseTeacher(@RequestBody CourseTeacher addCourseTeacher){
        log.info("添加教师：{}",addCourseTeacher);
        return null;
    }
}
