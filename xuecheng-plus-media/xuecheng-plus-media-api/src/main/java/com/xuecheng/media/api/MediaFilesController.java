package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFIleParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        if (pageParams.getPageNo() != null){
            return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);
        }else {
            return mediaFileService.queryMediaFielsWithoutPageParams(companyId,queryMediaParamsDto);
        }
    }

    @ApiOperation("上传图片")
    @PostMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile file,
                                      @RequestParam(value = "objectName", required = false)
                                      String objectName) throws IOException {
        UploadFIleParamsDto uploadFIleParamsDto = new UploadFIleParamsDto();
        uploadFIleParamsDto.setFilename(file.getOriginalFilename());
        uploadFIleParamsDto.setFileType("001001");
        if (!StringUtils.isEmpty(objectName)){
            uploadFIleParamsDto.setFileType("001003");
        }
        uploadFIleParamsDto.setFileSize(file.getSize());

        // 创建一个临时文件名为"minio"，后缀为".temp"
        File minio = File.createTempFile("minio", ".temp");
        try {
            // 将上传的 MultipartFile 的内容传输到临时文件中
            file.transferTo(minio);
            Long companyId = 1232141425L;
            String filepath = minio.getAbsolutePath();
            UploadFileResultDto uploadFileResultDto = mediaFileService.upload(companyId, uploadFIleParamsDto, filepath, objectName);
            return uploadFileResultDto;
        } finally {
            // 尝试删除临时文件
            if (!minio.delete()) {
                System.err.println("Failed to delete temporary file: " + minio.getAbsolutePath());
            }
        }
    }


    @DeleteMapping("/{mediaFilesId}")
    @ApiOperation("删除媒资文件")
    public void deleteMediaFiles(@PathVariable String mediaFilesId){
        mediaFileService.deleteMediaFiles(mediaFilesId);
    }

    @GetMapping("/preview")
    @ApiOperation("预览媒资文件")
    public void previewMediaFiles(){

    }
}
