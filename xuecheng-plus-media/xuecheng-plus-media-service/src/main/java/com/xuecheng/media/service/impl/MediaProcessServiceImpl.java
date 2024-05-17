package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Package:com.xuecheng.media.service.impl
 * @Auther:Brianwei
 * @date:2024/4/14:15:26
 * @discribe:
 */
@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;


    @Override
    public List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardId(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    @Override
    public boolean startTask(Long id) {
        int i = mediaProcessMapper.startTask(id);
        return i <= 0 ? false : true;
    }

    @Override
    public void saveMediaProcessFinishStatus(Long taskId, String status, String fileId, String url, String errMessage) {
        //要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        System.out.println(mediaProcess);
        if (mediaProcess == null){
            return;
        }
        //如果任务失败
        //更新状态，失败次数
        if (status.equals("3")) {
            //更高效的更新方式
            mediaProcess.setUrl(null);
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(errMessage);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        //如果任务成功

        //更新url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        System.out.println(url);
        mediaFilesMapper.updateById(mediaFiles);
        //更新mediaprocess的状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        //将process表记录插入到history表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistory.setId(null);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //将process表记录删除
        mediaProcessMapper.deleteById(taskId);
    }

}
