package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @Package:com.xuecheng.media.service
 * @Auther:Brianwei
 * @date:2024/4/14:15:26
 * @discribe:
 */
public interface MediaProcessService {

    /**
     * 查询待处理任务，不同的执行器传参不一样 查到的sql也不一样
     * @param shardTotal
     * @param shardIndex
     * @param count
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardTotal,int shardIndex,int count);

    boolean startTask(Long id);

    void saveMediaProcessFinishStatus(Long taskId,String status,String fileId,String url,String errMessage);
}
