package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Package:com.xuecheng.content.model.dto
 * @Auther:Brianwei
 * @date:2024/1/21:16:00
 * @discribe:
 */
@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class QueryCourseParamsDTO {

    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;
}
