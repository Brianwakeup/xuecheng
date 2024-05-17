package com.xuecheng.content.model.vo;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @Package:com.xuecheng.content.model.vo
 * @Auther:Brianwei
 * @date:2024/2/6:15:04
 * @discribe:
 */
@Data
public class TeachplanVO extends Teachplan {

    private TeachplanMedia teachplanMedia;

    //小章节list
    private List<TeachplanVO> teachPlanTreeNodes;
}
