package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/2/14:15:21
 * @discribe:
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public CourseTeacher list(Long id) {
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId,id);
        return courseTeacherMapper.selectOne(wrapper);
    }

    private CourseBase getCourseBase(Long id){
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (!courseBase.getCompanyId().equals(1232141425L)){
            XueChengPlusException.cast("只允许向机构自己的课程中添加老师");
        }
        return courseBase;
    }
}
