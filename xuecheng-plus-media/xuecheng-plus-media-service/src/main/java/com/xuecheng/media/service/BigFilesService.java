package com.xuecheng.media.service;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFIleParamsDto;

import java.io.File;

/**
 * @Package:com.xuecheng.media.service
 * @Auther:Brianwei
 * @date:2024/3/17:16:13
 * @discribe:
 */
public interface BigFilesService {
    RestResponse<Boolean> checkfile(String fileMd5);

    RestResponse<Boolean> checkchunk(String md5, int chunk);

    RestResponse uploadchunk(String md5, int chunk, String lcoalFileChunkPath);

    public File getFileStream(String bucketName, String objectName);

    RestResponse mergechunks(Long companyId, UploadFIleParamsDto uploadFIleParamsDto, String fileMd5, int chunkTotal);
}
