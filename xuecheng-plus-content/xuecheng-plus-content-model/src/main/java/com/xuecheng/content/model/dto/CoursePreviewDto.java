package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.vo.TeachplanVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Package:com.xuecheng.content.model.dto
 * @Auther:Brianwei
 * @date:2024/6/20:16:12
 * @discribe:
 */
@Data
@Builder
public class CoursePreviewDto {

    //课程基本信息，课程营销信息
    private CourseBaseInfoDto courseBase;

    //课程计划信息
    private List<TeachplanVO> teachplans;

    //课程师资信息


}
