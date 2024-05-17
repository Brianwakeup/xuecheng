package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFIleParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author brianwei
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    UploadFileResultDto upload(Long companyId, UploadFIleParamsDto uploadFIleParamsDto, String filepath);

    public MediaFiles addMediaFilesToDb(Long companyId, UploadFIleParamsDto uploadFIleParamsDto,String fileMd5
            ,String bucket,String objectName);

    public boolean addMeidaFileToMinio(String localFilePath, String mimeType, String bucket, String objectName);

    PageResult<MediaFiles> queryMediaFielsWithoutPageParams(Long companyId, QueryMediaParamsDto queryMediaParamsDto);
}
