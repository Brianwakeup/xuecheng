package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;
import java.net.URISyntaxException;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/6/20:16:20
 * @discribe: 课程发布相关的接口
 */
public interface CoursePublisService {

    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void commitAudit(Long companyId, Long courseId);

    void coursepublish(Long companyId,Long courseId);

    File generateCourseHtml(Long courseId);

    void uploadCourseHtml(Long courseId,File html);
}
