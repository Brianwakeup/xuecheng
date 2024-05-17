package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    List<MediaProcess> selectListByShardId(@Param("shardTotal") int shardTotal,@Param("shardIndex") int shardIndex,@Param("count") int count);


    /**
     * 开启一个任务
     * @param id
     * @return
     */
    @Update("Update media_process m set m.status = '4' where (m.status = '1' or m.status = '3') and m.id = #{id} and m.fail_count < 3")
    int startTask(@Param("id") Long id);
}
