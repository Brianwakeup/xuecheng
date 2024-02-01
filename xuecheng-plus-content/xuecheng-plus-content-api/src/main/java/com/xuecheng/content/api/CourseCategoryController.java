package com.xuecheng.content.api;

import com.xuecheng.content.model.vo.CourseCategoryTreeNodVO;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Package:com.xuecheng.content.api
 * @Auther:Brianwei
 * @date:2024/1/30:12:55
 * @discribe:
 */
@RestController
@Api(value = "课程分类管理接口",tags = "课程分类管理接口")
@RequestMapping("/course-category")
@Log4j2
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    public CourseCategoryController(CourseCategoryService courseCategoryService){
        this.courseCategoryService = courseCategoryService;
    }

    @GetMapping("/tree-nodes")
    @ApiOperation("课程分类查询接口")
    public List<CourseCategoryTreeNodVO> treenodes(){
        log.info("课程分类树状图查询");
        List<CourseCategoryTreeNodVO> courseCategoryTreeNodVO = courseCategoryService.getTreenodes();
        return courseCategoryTreeNodVO;
    }
}
