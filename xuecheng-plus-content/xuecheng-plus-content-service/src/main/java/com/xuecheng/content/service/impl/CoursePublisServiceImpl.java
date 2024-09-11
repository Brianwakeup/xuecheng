package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.model.vo.TeachplanVO;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublisService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/6/20:16:24
 * @discribe:
 */
@Service
@Slf4j
public class CoursePublisServiceImpl implements CoursePublisService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //查询课程基本信息以及营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getInfoBeforeUpdate(courseId);
        //查询课程计划信息
        List<TeachplanVO> teachPlanTree = teachplanService.getTeachPlan(courseId);
        CoursePreviewDto coursePreviewDto = CoursePreviewDto.builder()
                .courseBase(courseBaseInfo)
                .teachplans(teachPlanTree)
                .build();
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        log.info("{}号机构的{}课程提交审核",companyId,courseId);
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getInfoBeforeUpdate(courseId);
        //课程的图片 计划不能为空
        List<TeachplanVO> teachPlan = teachplanService.getTeachPlan(courseId);
        estimateDicition(companyId, courseBaseInfo,teachPlan);
        //查询到课程的基本信息 营销 计划 信息插入到课程预发布表里面
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转换为json格式
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //课程计划，转换为json格式
        String teachPlanJson = JSON.toJSONString(teachPlan);
        coursePublishPre.setTeachplan(teachPlanJson);
        //课程教师,转换为json格式
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId,courseId);
        CourseTeacher courseTeacher = courseTeacherMapper.selectOne(wrapper);
        String courseTeacherJson = JSON.toJSONString(courseTeacher);
        coursePublishPre.setTeachers(courseTeacherJson);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //审核状态改为已提交
        coursePublishPre.setStatus("202003");
        //判断一下如果预发布表为空则进行插入，如果不为空就进行更新
        if (coursePublishPreMapper.selectById(courseId) == null){
            //添加
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本信息表为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void coursepublish(Long companyId,Long courseId) {
        log.info("{}号机构的{}号课程发布",companyId,courseId);
        //将预发布表的信息写入到发布表中
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        //课程如果没有审核通过不允许发布
        if (!coursePublishPre.getStatus().equals("202004")){
            XueChengPlusException.cast("课程审核未通过，不允许发布");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //设置时间
        coursePublish.setOnlineDate(LocalDateTime.now());
        //修改状态
        coursePublish.setStatus("203002");
        //查询课程发布表，有就更新，没有就添加
        if (coursePublishMapper.selectById(courseId) == null){
            coursePublishMapper.insert(coursePublish);
        }else {
            coursePublishMapper.updateById(coursePublish);
        }
        //将这个课程发布的信息插入到消息表中,保证事物的一致性
        saveCoursePunlishMessage(courseId);
        //删除课程预发布表的记录
        coursePublishPreMapper.deleteById(courseId);
        //将课程状态改为已发布
        CourseBase courseBase = new CourseBase();
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File tempFile = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            String path = Paths.get(this.getClass().getResource("/templates").toURI()).toString();
            // 设置templates的路径
            configuration.setDirectoryForTemplateLoading(new File(path));
            configuration.setDefaultEncoding("utf-8");
            // 获取模板
            Template template = configuration.getTemplate("course_template.ftl");
            // 获取数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            // 组装数据
            HashMap<String, Object> model = new HashMap<>();
            model.put("model", coursePreviewInfo);
            // 处理模板，将模板内容转换为字符串
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            // 创建临时文件
            tempFile = File.createTempFile("coursePublish", ".html");
            inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            fileOutputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, fileOutputStream);
        }catch (Exception e){
            log.error("页面静态化出现问题,课程{}报错：{}",courseId,e.getMessage());
            XueChengPlusException.cast("页面静态化出现问题,课程" + courseId + "报错");
        }finally {
            try {
                inputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                log.error("html文件读取流或写入流关闭出现异常，异常原因：{}",e.getMessage());
                XueChengPlusException.cast("html文件读取流或写入流关闭出现异常");
            }
        }
        // 返回生成的临时文件
        return tempFile;
    }


    @Override
    public void uploadCourseHtml(Long courseId, File html) {
        //将接收到的文件上传到minio
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(html);
        try {
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (StringUtils.isEmpty(upload)){
                log.error("远程调用走了降级逻辑得到的结果为空，课程id：{}",courseId);
                XueChengPlusException.cast("上传静态文件过程当中出现异常");
            }
        }catch (Exception e){
            XueChengPlusException.cast(e.getMessage());
        }finally {
            html.delete();
        }

    }

    private void saveCoursePunlishMessage(Long courseId){
        MqMessage coursePublish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (coursePublish == null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR.getErrMessage());
        }
    }

    private void estimateDicition(long companyId, CourseBaseInfoDto courseBaseInfo,List<TeachplanVO> teachPlan) {
        if (courseBaseInfo == null){
            XueChengPlusException.cast("课程找不到");
        }
        if (teachPlan == null || teachPlan.isEmpty()){
            XueChengPlusException.cast("请编写课程计划");
        }
        if (StringUtils.isEmpty(courseBaseInfo.getPic())){
            XueChengPlusException.cast("请上传课程图片");
        }
        //如果课程的审核状态为已提交则不允许提交
        if (courseBaseInfo.getAuditStatus() .equals("202003")){
            XueChengPlusException.cast("课程已提交请等待审核");
        }
//        课程的机构id必须与提交的机构id一致
//        if (courseBaseInfo.getCompanyId() != companyId){
//            XueChengPlusException.cast("课程id必须与机构id一致");
//        }
    }
}
