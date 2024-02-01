package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.vo.CourseCategoryTreeNodVO;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/1/30:13:00
 * @discribe:
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private final CourseCategoryMapper courseCategoryMapper;

    public CourseCategoryServiceImpl(CourseCategoryMapper courseCategoryMapper){
        this.courseCategoryMapper = courseCategoryMapper;
    }

    @Override
    public List<CourseCategoryTreeNodVO> getTreenodes() {
        // 根节点id假设是"1"，您可以根据实际情况进行调整
        return buildTree("1");
    }

    private List<CourseCategoryTreeNodVO> buildTree(String parentId) {
        // 根据parentId查询子节点
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getParentid, parentId);
        List<CourseCategory> courseCategories = courseCategoryMapper.selectList(wrapper);

        // 如果没有子节点，返回空列表
        if (courseCategories.isEmpty()) {
            return null;
        }

        // 为每个子节点创建VO并递归构建其子树
        List<CourseCategoryTreeNodVO> treeNodes = new ArrayList<>();
        for (CourseCategory courseCategory : courseCategories) {
            CourseCategoryTreeNodVO treeNode = new CourseCategoryTreeNodVO(courseCategory);
            treeNode.setChildrenTreeNodes(buildTree(courseCategory.getId()));
            treeNodes.add(treeNode);
        }

        return treeNodes;
    }
}
