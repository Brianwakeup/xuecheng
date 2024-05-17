package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Package:com.xuecheng.content.model.dto
 * @Auther:Brianwei
 * @date:2024/2/5:8:20
 * @discribe:
 */
@Data
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "机构id",required = true)
    Long companyId;

    @ApiModelProperty(value = "课程id",required = true)
    Long id;
}
