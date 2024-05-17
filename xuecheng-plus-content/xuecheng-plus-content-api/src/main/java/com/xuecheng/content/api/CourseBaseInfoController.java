package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Package:com.xuecheng.content.api
 * @Auther:Brianwei
 * @date:2024/1/21:15:30
 * @discribe:
 */
@RestController
@Api(value = "课程信息编辑接口",tags = "课程信息管理接口")
@Log4j2
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false)
    QueryCourseParamsDTO queryCourseParamsDTO){
        log.info("进行分页查询");
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDTO);
        return pageResult;
    }

    @ApiOperation("课程新增接口")
    @PostMapping()
    //声明@Validated注解代表需要校验字段属性的约束注解
    public CourseBaseInfoDto addCourse(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){
        log.info("进行课程信息的添加：{}",addCourseDto);
        //获取到用户所属机构的id
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.createCourseBase(1L,addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("get infomation before update the course")
    @GetMapping("/{id}")
    public CourseBaseInfoDto getInfo(@PathVariable("id") Long id){
        log.info("查询课程信息：{}",id);
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getInfoBeforeUpdate(id);
        return courseBaseInfoDto;
    }

    @ApiOperation("update course infomation")
    @PutMapping()
    public CourseBaseInfoDto updateCourseInfo(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        log.info("对课程信息进行更新：{}",editCourseDto);
        CourseBaseInfoDto courseBaseInfo =  courseBaseInfoService.updateCourseInfo(editCourseDto);
        return courseBaseInfo;
    }
}
