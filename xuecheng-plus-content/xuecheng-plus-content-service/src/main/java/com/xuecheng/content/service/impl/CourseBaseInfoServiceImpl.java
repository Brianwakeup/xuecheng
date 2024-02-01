package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/1/28:11:24
 * @discribe:
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    private final CourseBaseMapper courseBaseMapper;

    public CourseBaseInfoServiceImpl(CourseBaseMapper courseBaseMapper){
        this.courseBaseMapper = courseBaseMapper;
    }

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDTO queryCourseParamsDTO) {
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryCourseParamsDTO.getCourseName()),CourseBase::getName,queryCourseParamsDTO.getCourseName());
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDTO.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDTO.getAuditStatus());
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDTO.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDTO.getPublishStatus());
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, wrapper);
        PageResult<CourseBase> pageResult = PageResult.<CourseBase>builder()
                .items(courseBasePage.getRecords())
                .page(pageParams.getPageNo())
                .pageSize(pageParams.getPageSize())
                .counts(courseBasePage.getTotal())
                .build();
        return pageResult;
    }
}
