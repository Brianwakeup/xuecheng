package com.xuecheng.content.model.vo;

import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Package:com.xuecheng.content.model.vo
 * @Auther:Brianwei
 * @date:2024/1/30:12:51
 * @discribe:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseCategoryTreeNodVO extends CourseCategory implements Serializable {

    public CourseCategoryTreeNodVO(CourseCategory courseCategory){
        this.setId(courseCategory.getId());
        this.setName(courseCategory.getName());
        this.setLabel(courseCategory.getLabel());
        this.setOrderby(courseCategory.getOrderby());
        this.setParentid(courseCategory.getParentid());
        this.setIsLeaf(courseCategory.getIsLeaf());
        this.setIsShow(courseCategory.getIsShow());
        this.setChildrenTreeNodes(null);
    }

    List<CourseCategoryTreeNodVO> childrenTreeNodes;
}
