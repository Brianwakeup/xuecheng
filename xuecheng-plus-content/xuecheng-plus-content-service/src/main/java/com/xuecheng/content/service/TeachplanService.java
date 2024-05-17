package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.model.vo.BindTeachplanMediaVO;
import com.xuecheng.content.model.vo.TeachplanVO;

import java.util.List;

/**
 * @Package:com.xuecheng.content.service
 * @Auther:Brianwei
 * @date:2024/2/6:15:20
 * @discribe:
 */
public interface TeachplanService {
    List<TeachplanVO> getTeachPlan(Long id);

    List<TeachplanVO> findTeachPlanTree(Long id);

    void deleteTeachPlan(Long id);

    void movedown(Long id);

    void moveup(Long id);

    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    TeachplanMedia associationMedia(BindTeachplanMediaVO bindTeachplanMediaVO);
}
