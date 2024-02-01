package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.vo.CourseCategoryTreeNodVO;

import java.util.List;

/**
 * @Package:com.xuecheng.content.service
 * @Auther:Brianwei
 * @date:2024/1/30:12:59
 * @discribe:
 */
public interface CourseCategoryService{
    List<CourseCategoryTreeNodVO> getTreenodes();
}
