package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFIleParamsDto;
import com.xuecheng.media.service.BigFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

/**
 * @Package:com.xuecheng.media.api
 * @Auther:Brianwei
 * @date:2024/3/5:10:05
 * @discribe: 上传视频
 */
@RestController
@Api(value = "大文件上传接口",tags = "大文件上传接口")
@Slf4j
@RequestMapping("/upload")
public class BigFilesController {

    @Autowired
    BigFilesService bigFilesService;

    Long companyId = 1232141425l;

    /**
     * 检查文件是否存在
     * @param fileMd5
     * @return
     */
    @ApiOperation("文件上传前检查文件")
    @PostMapping("/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5){
        RestResponse<Boolean> isexit = bigFilesService.checkfile(fileMd5);
        return isexit;
    }

    @ApiOperation("分块文件上传前的检测")
    @PostMapping("/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String md5,
                                            @RequestParam("chunk") int chunk){
        RestResponse<Boolean> isexit = bigFilesService.checkchunk(md5,chunk);
        return isexit;
    }

    @ApiOperation("上传分块文件")
    @PostMapping("/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("fileMd5") String md5,
                                             @RequestParam("chunk") int chunk,
                                             @RequestParam("file")MultipartFile file) throws IOException {
        File minio = File.createTempFile("minio", ".temp");
        file.transferTo(minio);
        String absolutePath = minio.getAbsolutePath();
        RestResponse response = bigFilesService.uploadchunk(md5,chunk,absolutePath);
        minio.delete();
        return response;
    }

    @ApiOperation("合并文件")
    @PostMapping("/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileName") String fileName,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunkTotal")int chunkTotal){
        UploadFIleParamsDto uploadFIleParamsDto = new UploadFIleParamsDto();
        uploadFIleParamsDto.setFilename(fileName);
        uploadFIleParamsDto.setFileType("001002");
        uploadFIleParamsDto.setTags("视频文件");
        RestResponse response = bigFilesService.mergechunks(companyId,uploadFIleParamsDto,fileMd5,chunkTotal);
        return response;
    }
}
