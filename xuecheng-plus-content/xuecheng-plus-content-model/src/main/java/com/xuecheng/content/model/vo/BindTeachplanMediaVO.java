package com.xuecheng.content.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Package:com.xuecheng.content.model.vo
 * @Auther:Brianwei
 * @date:2024/4/25:8:20
 * @discribe:
 */
@Data
@ApiModel(value = "BindTeachplanMediaVO",description = "教学计划-媒资绑定提交数据")
public class BindTeachplanMediaVO {

    @ApiModelProperty(value = "媒资文件id",required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称",required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识",required = true)
    private Long teachplanId;
}
