package com.xuecheng.content.service.xxlJob;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublisService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Queue;

/**
 * @Package:com.xuecheng.content.service.xxlJob
 * @Auther:Brianwei
 * @date:2024/7/28:15:03
 * @discribe: 课程发布任务类
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {


    @Autowired
    CoursePublisService coursePublisService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void CoursePublishJobHandler() throws Exception{
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //调用抽象类的方法执行任务
        process(shardIndex,shardTotal,"course_publish",30,60L);
    }

    //执行课程发布任务的逻辑,如果此方法抛出异常代表执行失败
    @Override
    public boolean execute(MqMessage mqMessage) {
            Long businessKey1 = Long.parseLong(mqMessage.getBusinessKey1());
            //课程静态化上传到minio
            generateCourseHtml(mqMessage,businessKey1);
            //写入索引到elsticsearch
            indexElsticsearch(mqMessage,businessKey1);
            //缓存到redis
            cacheReids(mqMessage,businessKey1);
            return true;
    }


    //生成静态化页面并上传到系统
    private void generateCourseHtml(MqMessage mqMessage,Long courseId){
        //先对任务的状态进行判断
        //取出消息id
        Long id = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0){
            //不进行处理
            log.debug("课程静态化任务完成，无需处理");
        }else {
            //开始进行课程静态化
            File html = coursePublisService.generateCourseHtml(courseId);
            if (html == null){
                XueChengPlusException.cast("生成的页面为空");
            }
            //上传静态化页面到minio
            coursePublisService.uploadCourseHtml(courseId,html);
            //处理完成，修改状态
            mqMessageService.completedStageOne(id);
        }
    }

    private void indexElsticsearch(MqMessage mqMessage,Long courseId){
        Long id = mqMessage.getId();
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo > 0){
            log.debug("上传索引到elsticsearch任务完成，无需处理");
        }else {
            mqMessageService.completedStageTwo(id);
        }
    }

    private void cacheReids(MqMessage mqMessage,Long courseId){
        Long id = mqMessage.getId();
        int stageTwo = mqMessageService.getStageThree(id);
        if (stageTwo > 0){
            log.debug("写入缓存到redis任务完成，无需处理");
        }else {
            mqMessageService.completedStageThree(id);
        }
    }
}
