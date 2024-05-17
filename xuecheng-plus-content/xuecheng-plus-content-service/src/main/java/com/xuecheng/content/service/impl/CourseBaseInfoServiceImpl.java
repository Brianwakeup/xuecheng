package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.Source;
import java.time.LocalDateTime;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/1/28:11:24
 * @discribe:
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;

    public CourseBaseInfoServiceImpl(CourseBaseMapper courseBaseMapper,CourseMarketMapper courseMarketMapper){
        this.courseBaseMapper = courseBaseMapper;
        this.courseMarketMapper = courseMarketMapper;
    }

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

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

    @Override
    @Transactional //增删改的方法都要加上事务注解
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        //参数合法性校验，目前省略，到后面通过注解进行调用
        //向课程基本信息表写入数据
        CourseBase courseBase = new CourseBase();
        //将传入页面的参数传入对象当中
//        courseBase.builder()
//                .name(addCourseDto.getName());
        //但是这种方法太复杂了，直接beanutils copy
        BeanUtils.copyProperties(addCourseDto,courseBase); //只要属性名称一致就可以进行copy
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0){
            XueChengPlusException.cast("添加课程失败");
        }
        //向课程营销系统表写入数据
        CourseMarket courseMarket = new CourseMarket();
        //课程已插入成功，id就有了
        courseMarket.setId(courseBase.getId());
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        int i = saveCourseMarket(courseMarket);
        if (i <= 0){
            XueChengPlusException.cast("课程营销信息更新失败");
        }
        //从数据库查询课程的详细信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBase.getId());
        return courseBaseInfo;
    }

    @Override
    public CourseBaseInfoDto getInfoBeforeUpdate(Long id) {
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(id);
        if (courseBaseInfo == null){
            XueChengPlusException.cast("课程不存在");
        }else {
            return courseBaseInfo;
        }
        return courseBaseInfo;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseInfo(EditCourseDto editCourseDto) {
        //校验数据合法性
        CourseBase courseBase = courseBaseMapper.selectById(editCourseDto.getId());
        if (courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        //非一般性的参数校验，具有业务逻辑的，要写在service
        if (!courseBase.getCompanyId().equals(editCourseDto.getCompanyId())){
            //机构不符合
            XueChengPlusException.cast("机构不符合");
        }
        //封装数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        //修改时间
        courseBase.setChangeDate(LocalDateTime.now());
        //修改人
        System.out.println(courseBase);
        int i = courseBaseMapper.updateById(courseBase);
        if (i <= 0){
            XueChengPlusException.cast("课程更新失败");
        }
        //更新营销表
        CourseMarket courseMarket = new CourseMarket();
        courseMarket.setId(courseBase.getId());
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        int i1 = saveCourseMarket(courseMarket);
        if (i1 <= 0){
            XueChengPlusException.cast("课程营销表更新失败");
        }
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBase.getId());
        return courseBaseInfo;
    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null){
            courseMarket = new CourseMarket();
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        //解决大分类，小分类
        String mt = courseBase.getMt();
        String st = courseBase.getSt();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        //set up name
        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(mt);
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(st);
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());
        courseBaseInfoDto.setStName(courseCategorySt.getName());
        return courseBaseInfoDto;
    }

    //单独写一个方法，存在更新，不存在则插入
    private int saveCourseMarket(CourseMarket courseMarket){

        //进行参数的合法性校验
        if (StringUtils.isEmpty(courseMarket.getCharge())){
            XueChengPlusException.cast("收费规则为空");
        }
        if (courseMarket.getCharge().equals("201000")){
            if (courseMarket.getPrice() != null || courseMarket.getOriginalPrice() != null){
                XueChengPlusException.cast("课程免费的情况下不能设置价格");
            }
        }
        //如果课程收费，价格没有填写也要抛出异常
        if (courseMarket.getCharge().equals("201001")){
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0){
                XueChengPlusException.cast("课程的价格不能为空并且必须大于0");
            }
        }
        //存在则更新，不存在则插入
        CourseMarket market = courseMarketMapper.selectById(courseMarket.getId());
        if (market != null){
            //存在,更新
            return courseMarketMapper.updateById(courseMarket);
        }else {
            //不存在，插入
            return courseMarketMapper.insert(courseMarket);
        }
    }
}
