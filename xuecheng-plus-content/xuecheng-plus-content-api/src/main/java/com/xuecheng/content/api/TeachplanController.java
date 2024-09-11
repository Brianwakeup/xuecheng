package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.model.vo.BindTeachplanMediaVO;
import com.xuecheng.content.model.vo.TeachplanVO;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Package:com.xuecheng.content.api
 * @Auther:Brianwei
 * @date:2024/2/6:15:07
 * @discribe: 课程计划管理相关接口
 */
@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@Slf4j
@RequestMapping("/teachplan")
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @GetMapping("/{id}/tree-nodes")
    @ApiOperation("查看课程计划树形结构")
    public List<TeachplanVO> treenodes(@PathVariable("id") Long id){
        log.info("{}号课程完成添加或修改，进行教学计划的查询",id);
        List<TeachplanVO> teachPlan = teachplanService.getTeachPlan(id);
        return teachPlan;
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除课程计划")
    public void dropTeachPlan(@PathVariable("id") Long id){
        log.info("删除课程计划id：{}",id);
        teachplanService.deleteTeachPlan(id);
    }

    @PostMapping("/movedown/{id}")
    @ApiOperation("将课程计划下移")
    public void movedownTeachPlan(@PathVariable("id") Long id){
        log.info("{}号课程计划下移");
        teachplanService.movedown(id);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @PostMapping("/moveup/{id}")
    @ApiOperation("/将课程计划上移")
    public void moveupTeachPlan(@PathVariable("id") Long id){
        log.info("{}号课程计划上移");
        teachplanService.moveup(id);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public void association(@RequestBody BindTeachplanMediaVO bindTeachplanMediaVO){
        teachplanService.associationMedia(bindTeachplanMediaVO);
    }

    @ApiOperation(value = "课程计划和媒资信息删除")
    @DeleteMapping("/association/media/{teachPlanId}/{mediaId}")
    public void association(@PathVariable Long teachPlanId,@PathVariable String mediaId){
        teachplanService.deleteAssociationMedia(teachPlanId,mediaId);
    }
}
