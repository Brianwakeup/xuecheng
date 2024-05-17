package com.xuecheng.content.mapper;

import com.xuecheng.content.model.po.Teachplan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.vo.TeachplanVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanVO> getTeachPlanVOByParentId(Long id);

    List<TeachplanVO> selectTreeNodes(Long id);

    List<Teachplan> selectByParentId(Long id);
}
