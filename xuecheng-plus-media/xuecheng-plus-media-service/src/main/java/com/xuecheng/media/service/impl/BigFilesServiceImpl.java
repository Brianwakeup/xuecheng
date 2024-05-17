package com.xuecheng.media.service.impl;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.UploadFIleParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.BigFilesService;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Package:com.xuecheng.media.service.impl
 * @Auther:Brianwei
 * @date:2024/3/17:16:13
 * @discribe:
 */
@Service
@Slf4j
public class BigFilesServiceImpl implements BigFilesService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileServiceImpl mediaFileService;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        //先查询数据库当中有没有文件存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null){
            //再查询minio中文件是否存在
            //桶
            String bucket = mediaFiles.getBucket();
            //存储目录
            String filePath = mediaFiles.getFilePath();
            InputStream object = null;
            try {
                //如果中间出了问题就会报空指针
                object = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(filePath)
                                .build()
                );
                System.out.println(object);
                //如果存在就返回true
                if (object != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }else {
            //不存在返回false 进行下一步
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkchunk(String md5, int chunkindex) {
        //分块存储路径是md5前两位为两个目录，chunk存储分块文件
        //根据md5值得到文件路径
        String path = getChunkFileFolderPath(md5);
        try {
            InputStream object = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket_videofiles)
                    .object(path + chunkindex)
                    .build());
            if (object != null){
                //分块存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadchunk(String md5, int chunk, String localFileChunkPath) {
        //分块文件的路径
        String chunkFileFolderPath = getChunkFileFolderPath(md5) + chunk;
        //获取minitype
        String mimeType = MediaFileServiceImpl.getMimeType(null);
        //将分块文件上传到minio
        boolean b = mediaFileService.addMeidaFileToMinio(localFileChunkPath, mimeType, bucket_videofiles, chunkFileFolderPath);
        if (!b){
            return RestResponse.validfail(false,"上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, UploadFIleParamsDto uploadFIleParamsDto, String fileMd5, int chunkTotal) {
        //合并文件
        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //set up the source
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder()
                        .bucket(bucket_videofiles)
                        .object(chunkFileFolderPath + i)
                        .build())
                .collect(Collectors.toList());
        //源文件名称
        String filename = uploadFIleParamsDto.getFilename();
        //扩展名
        String extention = filename.substring(filename.lastIndexOf("."));
        //minio存储地址
        String objectname = getFileFolderPath(fileMd5,extention);
        //merge file
        try {
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket_videofiles)
                            .sources(sources)
                            .object(objectname).build()
            );
            //合并成功
            log.info("文件{}合并成功",objectname);
        }catch (Exception e){
            log.error("合并文件出错：bucket：{}，object：{}",bucket_videofiles,objectname);
            return RestResponse.validfail(false,"文件合并失败");
        }
        //校验合并后的文件和源文件是否一致，md5
        File file = getFileStream(bucket_videofiles, objectname);
        //try里自动关闭流
        try (InputStream inputStream = Files.newInputStream(file.toPath())){
            String fileStreamMd5 = DigestUtils.md5DigestAsHex(inputStream);
            if (!fileStreamMd5.equals(fileMd5)){
                //不一致
                log.error("校验合并文件信息不一致");
                return RestResponse.validfail(false,"合并文件失败，信息不一致");
            }
        }catch (Exception e){
            return RestResponse.validfail(false,"合并文件失败，信息不一致");
        }
        //上传成功了才会入库，所以不同担心上传失败再传入数据库
        //文件信息入库
        uploadFIleParamsDto.setFileSize(file.length());
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId,
                uploadFIleParamsDto, fileMd5, bucket_videofiles, objectname);
        if (mediaFiles == null){
            //先删除minio文件
            //分块文件不删除，因为下次上传视频数据库说不定就添加成功了，接着合并就可以
            log.error("合并文件成功但是保存文件信息失败，删除合并文件");
            return RestResponse.validfail(false,"文件合并后保存文件信息失败");
        }
        //清理分块文件
        clearFileChunk(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true,"文件合并成功");
    }


    public File getFileStream(String bucketName, String objectName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            File tempFile = File.createTempFile("minio", "temp");
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                IOUtils.copy(stream, fileOutputStream);
            }
            return tempFile;
        } catch (Exception e) {
            log.error("获取文件失败", e);
        }
        return null;
    }

    private String getChunkFileFolderPath(String md5) {
        return md5.substring(0,1) + "/" + md5.substring(1,2) + "/" + md5 + "/" + "chunk" + "/";
    }

    private String getFileFolderPath(String md5,String extention) {
        return md5.substring(0,1) + "/" + md5.substring(1,2) + "/" + md5 + "/" + md5 + extention;
    }

    private void clearFileChunk(String chunkFloderPath,int chunkTotal){
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFloderPath.concat(i.toString())))
                    .collect(Collectors.toList());
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucket_videofiles)
                    .objects(deleteObjects)
                    .build());
            results.forEach(re -> {
                DeleteError deleteError = null;
                try {
                    deleteError = re.get();
                } catch (Exception e) {
                    log.error("清除分块文件失败,分块文件：{}", deleteError.objectName());
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("清除分块文件失败：分块文件路径：{}",chunkFloderPath);
        }
    }
}
