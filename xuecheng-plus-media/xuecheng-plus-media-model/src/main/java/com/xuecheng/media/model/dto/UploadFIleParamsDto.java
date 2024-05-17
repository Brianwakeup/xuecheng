package com.xuecheng.media.model.dto;

import lombok.Data;

/**
 * @Package:com.xuecheng.media.model.dto
 * @Auther:Brianwei
 * @date:2024/2/26:14:10
 * @discribe: 文件信息模型类
 */
@Data
public class UploadFIleParamsDto {

    //文件名
    private String filename;

    //文件类型
    private String fileType;

    //标签
    private String tags;

    //上传人
    private String username;

    //备注
    private String remark;

    //文件大小
    private Long fileSize;
}
