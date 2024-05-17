package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @Package:com.xuecheng.content.service
 * @Auther:Brianwei
 * @date:2024/1/28:11:21
 * @discribe: 课程信息管理接口
 */
public interface CourseBaseInfoService {

    //课程分页查询
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDTO queryCourseParamsDTO);


    /**
     * 新增课程
     * @param companyI 机构id
     * @param addCourseDto 课程信息
     * @return 课程详细信息
     */
    CourseBaseInfoDto createCourseBase(Long companyI,AddCourseDto addCourseDto);

    CourseBaseInfoDto getInfoBeforeUpdate(Long id);

    CourseBaseInfoDto updateCourseInfo(EditCourseDto editCourseDto);
}
