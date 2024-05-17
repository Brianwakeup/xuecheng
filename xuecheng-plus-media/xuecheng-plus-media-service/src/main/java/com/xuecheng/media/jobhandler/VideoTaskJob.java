package com.xuecheng.media.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.base.utils.VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.BigFilesService;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Package:com.xuecheng.media.jobhandler
 * @Auther:Brianwei
 * @date:2024/4/19:8:13
 * @discribe:
 */
@Slf4j
@Component
public class VideoTaskJob {

    @Autowired
    MediaProcessService mediaProcessService;

    @Autowired
    BigFilesService bigFilesService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        //执行器序号
        int shardIndex = XxlJobHelper.getShardIndex();
        //执行器总数
        int shardTotal = XxlJobHelper.getShardTotal();

        //确定cpu的核心数
        int cpu = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务，一次最多只能处理这些任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardTotal, shardIndex, cpu);
        //可以处理的任务数量
        int size = mediaProcessList.size();
        log.info("取到的视频任务处理数：{}",size);
        if (size <= 0){
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //创建线程池进行处理
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(() -> {
                try {
                    //任务id
                    Long id = mediaProcess.getId();
                    //抢占任务
                    boolean b = mediaProcessService.startTask(id);
                    if (!b) {
                        log.debug("抢占任务失败，任务id:{}", id);
                        return;
                    } else {
                        log.info("抢占任务成功");
                    }
                    //md5
                    String fileId = mediaProcess.getFileId();
                    String bucket = mediaProcess.getBucket();
                    //文件的minio路径，objectname
                    String filePath = mediaProcess.getFilePath();
                    //下载视频到本地
                    File fileStream = bigFilesService.getFileStream(bucket, filePath);
                    if (fileStream == null) {
                        log.debug("下载视频出错，视频id:{}", id);
                        mediaProcessService.saveMediaProcessFinishStatus(id, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    System.out.println("fileStream" + fileStream);
                    //原视频avi路径
                    String aviPath = fileStream.getAbsolutePath();
                    //转换后的视频名称
                    String mp4Name = fileId + ".mp4";
                    File minio = null;
                    //创建临时文件，转换视频
                    try {
                        minio = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常：{}", e.getMessage());
                        mediaProcessService.saveMediaProcessFinishStatus(id, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4Path = minio.getAbsolutePath();
                    System.out.println(mp4Path);
                    //创建工具类对象开始视频转换，原avi视频的路径，转换后的mp4视频的路径
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegpath, aviPath, mp4Path);
                    String s = mp4VideoUtil.generateMp4();
                    //成功返回success，失败返回失败原因
                    if (!s.equals("success")) {
                        log.info("视频转码失败,bucket:{},onjectName:{},失败原因:{}", bucket, mp4Name, s);
                        mediaProcessService.saveMediaProcessFinishStatus(id, "3", fileId, null, s);
                        return;
                    }
                    //修改后的minio路径
                    filePath = filePath.substring(0,filePath.lastIndexOf(".")) + ".mp4";
                    System.out.println(filePath);
                    //上传到minio mp4Path本地文件路径，filePath minio文件路径
                    boolean b1 = mediaFileService.addMeidaFileToMinio(mp4Path, "video/mp4", bucket, filePath);
                    if (!b1) {
                        log.debug("上传mp4到minio失败,taskId:{}", id);
                        mediaProcessService.saveMediaProcessFinishStatus(id, "3", fileId, null, "上传文mp4到minio失败");
                        return;
                    }
                    //mp4的url
                    String url = getFilePath("/" + bucket + "/" + fileId, ".mp4");
                    //上传成功了，更新信息
                    minio.delete();     //删除临时文件
                    fileStream.delete();        //删除下载minio avi视频文件
                    mediaProcessService.saveMediaProcessFinishStatus(id, "2", fileId, url, "");
                }finally {
                    countDownLatch.countDown();
                }
            });
        });
        //阻塞，指定最大的等待时间，阻塞最多等待一定时间后就
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String md5,String fileExt){
        return md5.substring(0,1) + "/" + md5.substring(1,2) + "/" + md5 + "/" + md5 + fileExt;
    }
}
