package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFIleParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.Version;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Autowired
    private MediaFileService current;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

    /**
     *
     * @param companyId
     * @param uploadFIleParamsDto
     * @param filepath
     * @return
     */
    @Override
    public UploadFileResultDto upload(Long companyId, UploadFIleParamsDto uploadFIleParamsDto, String filepath,String objectName) {
        //先得到扩展名
        String filename = uploadFIleParamsDto.getFilename();
        String extensionName = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extensionName);
        //先获取要存入的文件路径
        String defalutFolderPath = getDefalutFolderPath();
        //拿到文件的md5值再进行拼接
        String md5 = getMd5(filepath);
        //再得到obj文件名,如果没传，那么就是年月日路径
        if (StringUtils.isEmpty(objectName)){
            objectName = defalutFolderPath + md5 + extensionName;
        }
        //将文件上传到minio
        //如果网络请求的时间长了，那么事务就会占用数据库的资源，极端情况下导致数据库连接不够用，所以需要进行事务优化
        //但是非事务方法调用同类的一个事务方法，事务无法控制
        boolean b = addMeidaFileToMinio(filepath, mimeType, bucket_files, objectName);
        if (!b) {
            XueChengPlusException.cast("上传文件失败");
        }
        //成功了才会写入数据库
        //将文件信息保存到数据库
        //因为接口注入的是代理对象，所以自己注入自己的实现类可以解决在非事务方法中调用事务方法无法控制事务的情况
        MediaFiles mediaFiles = current.addMediaFilesToDb(companyId, uploadFIleParamsDto, md5, bucket_files, objectName);
        if (mediaFiles == null){
            XueChengPlusException.cast("文件上传后保存文件失败");
        }
        //返回一个文件信息
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFIleParamsDto uploadFIleParamsDto,String fileMd5
    ,String bucket,String objectName){
        //将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFIleParamsDto,mediaFiles);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setTags(uploadFIleParamsDto.getTags());
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0){
                log.error("向数据库中保存文件信息失败：{}",mediaFiles);
                return null;
            }
            return mediaFiles;
        }
        addWaitingTask(mediaFiles);

        return mediaFiles;
    }

    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        String extention = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extention);
        if (mimeType.equals("video/x-msvideo")){
            //写入数据库
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            //状态 未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    String getMd5(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            XueChengPlusException.cast("文件路径不可以为空");
        }
        try {
            String md5Hex = DigestUtils.md5Hex(new FileInputStream(new File(filepath)));
            return md5Hex;
        } catch (IOException e) {
            log.error("文件路径错误");
        }
        return null;
    }

    String getDefalutFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String folder = simpleDateFormat.format(new Date()) + "/";
        return folder;
    }

    public boolean addMeidaFileToMinio(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .object(objectName)
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.info("上传文件到minio成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错：bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
        }
        return false;
    }

    @Override
    public PageResult<MediaFiles> queryMediaFielsWithoutPageParams(Long companyId, QueryMediaParamsDto queryMediaParamsDto) {
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        List<MediaFiles> mediaFiles = mediaFilesMapper.selectList(wrapper);
        PageResult<MediaFiles> result = new PageResult<>(mediaFiles,Long.valueOf(mediaFiles.size()),null,null);
        return result;
    }

    @Override
    public String getMediaFileUrlById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        if (mediaFiles == null || mediaFiles.getUrl() == null){
            return null;
        }
        return mediaFiles.getUrl();
    }

    @Override
    @Transactional
    public void deleteMediaFiles(String mediaFilesId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaFilesId);
        //先删除minio文件，再删除表数据
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(mediaFiles.getBucket())
                            .object(mediaFiles.getFilePath())
                            .build());
        } catch (Exception e){
            XueChengPlusException.cast(e.getMessage());
        }
        //删除成功
        //删除表数据
        int i = mediaFilesMapper.deleteById(mediaFilesId);
        if (i <= 0){
            XueChengPlusException.cast("删除数据失败");
        }
    }

    static String getMimeType(String extention) {
        if (StringUtils.isEmpty(extention)) {
            extention = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extention);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;  //通用mimetype 字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
}
