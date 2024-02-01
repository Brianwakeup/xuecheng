package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Package:com.xuecheng.content
 * @Auther:Brianwei
 * @date:2024/1/26:17:06
 * @discribe:
 */
@SpringBootTest
public class CourseBaseMapperTest {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper(){

        //查询条件
        QueryCourseParamsDTO queryCourseParamsDTO = new QueryCourseParamsDTO();
        queryCourseParamsDTO.setCourseName("java"); //课程名称插叙条件

        //拼装查询条件
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        //根据名称进行模糊查询
        //1.条件，不为空则进行后面 2.要查询的字段 3.要查询的值
        wrapper.like(StringUtils.isNotEmpty(queryCourseParamsDTO.getCourseName()),CourseBase::getName,queryCourseParamsDTO.getCourseName());

        //根据课程的审核状态进行查询
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDTO.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDTO.getAuditStatus());

        //根据课程的发布状态进行查询
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDTO.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDTO.getPublishStatus());
        //参数1.当前页码 2.每页记录数
        Page<CourseBase> page = new Page<>();
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(10L);
        page.setSize(pageParams.getPageSize());
        page.setCurrent(pageParams.getPageNo());
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, wrapper);
        PageResult<CourseBase> pageResult = new PageResult<>(courseBasePage.getRecords(),courseBasePage.getTotal(),pageParams.getPageNo(),pageParams.getPageSize());
    }
}
