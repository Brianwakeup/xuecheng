package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Package:com.xuecheng.media.api
 * @Auther:Brianwei
 * @date:2024/6/24:15:11
 * @discribe:
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlBuMediaId(@PathVariable String mediaId){
        String url = mediaFileService.getMediaFileUrlById(mediaId);
        if (StringUtils.isEmpty(url)){
            return RestResponse.validfail("没有当前媒资文件或文件无路径");
        }
        return RestResponse.success(url);
    }
}
