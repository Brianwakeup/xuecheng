package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.model.vo.BindTeachplanMediaVO;
import com.xuecheng.content.model.vo.TeachplanVO;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Package:com.xuecheng.content.service.impl
 * @Auther:Brianwei
 * @date:2024/2/6:15:25
 * @discribe:
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    public List<TeachplanVO> getTeachPlan(Long id) {
        if (courseBaseMapper.selectById(id) == null){
            XueChengPlusException.cast("当前课程不存在");
        }
        //先查询课程的大章节
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Teachplan::getOrderby);
        wrapper.eq(Teachplan::getCourseId,id);
        wrapper.eq(Teachplan::getParentid,0L);
        List<Teachplan> teachplans = teachplanMapper.selectList(wrapper);
        //课程的大章节
        ArrayList<TeachplanVO> teachplanVOS = new ArrayList<>();
        for (Teachplan teachplan : teachplans) {
            TeachplanVO teachplanVO = new TeachplanVO();
            BeanUtils.copyProperties(teachplan,teachplanVO);
            teachplanVO.setTeachplanMedia(teachplanMediaMapper.getTeachplanMediaByTeachPlanId(teachplan.getId()));
            //查询大章节下的小章节
            List<TeachplanVO> teachPlanVOByParentId = teachplanMapper.getTeachPlanVOByParentId(teachplanVO.getId());
            teachPlanVOByParentId.forEach(teachplanVO1 -> {
                teachplanVO1.setTeachplanMedia(teachplanMediaMapper.getTeachplanMediaByTeachPlanId(teachplanVO1.getId()));
            });
            teachplanVO.setTeachPlanTreeNodes(teachPlanVOByParentId);
            teachplanVOS.add(teachplanVO);
        }
        //再查询课程的小章节
        return teachplanVOS;
    }

    @Override
    public List<TeachplanVO> findTeachPlanTree(Long id) {
        List<TeachplanVO> teachplanVOS = teachplanMapper.selectTreeNodes(id);
        return teachplanVOS;
    }

    @Override
    @Transactional
    public void deleteTeachPlan(Long id) {
        Teachplan teachplan = nullOfTeachPlanAndCourse(id);
        if (teachplan.getParentid().equals(0L)){
            //查询其下是否有课程
            List<Teachplan> teachplans  = teachplanMapper.selectByParentId(id);
            if (teachplans.size() != 0){
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
        }
        //先删除课程信息
        int i = teachplanMapper.deleteById(id);
        //先查询是否有媒体信息再进行删除媒体信息
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId,id);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(wrapper);
        if (teachplanMedia != null){
            int delete = teachplanMediaMapper.delete(wrapper);
            if (delete <= 0 || i <= 0){
                XueChengPlusException.cast("删除课程计划失败");
            }
        }else {
            if (i <= 0){
                XueChengPlusException.cast("删除课程计划失败");
            }
        }
    }

    @Override
    @Transactional
    public void movedown(Long id) {
        //先查询，不为null再进行操作
        Teachplan teachplan = nullOfTeachPlanAndCourse(id);

        //判断当前教学计划是否为章节或者小节
        if (teachplan.getParentid().equals(0L)){
            //如果当前教学计划在最后，那么不能进行下降
            LambdaQueryWrapper<Teachplan> last = new LambdaQueryWrapper<>();
            last.eq(Teachplan::getCourseId,teachplan.getCourseId());
            last.orderByAsc(Teachplan::getOrderby);
            List<Teachplan> teachplansOrderBy = teachplanMapper.selectList(last);
            Teachplan teachplan2 = teachplansOrderBy.get(teachplansOrderBy.size() - 1);
            if (teachplan2.getOrderby().equals(teachplan.getOrderby())){
                XueChengPlusException.cast("当前教学计划已在最下方");
            }
            //当前教学计划为章节,获取全部章节
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
            wrapper.eq(Teachplan::getParentid,0L);
            List<Teachplan> teachplans = teachplanMapper.selectList(wrapper);
            List<Teachplan> collect = teachplans.stream()
                    .filter(teachplan1 -> teachplan1.getOrderby() == teachplan.getOrderby() + 1)
                    .collect(Collectors.toList());
            Teachplan teachplanDown = collect.get(0);
            //下面的元素上移
            teachplanDown.setOrderby(teachplanDown.getOrderby() - 1);
            //下面的元素下移
            teachplan.setOrderby(teachplan.getOrderby() + 1);
            teachplanDown.setChangeDate(LocalDateTime.now());
            teachplan.setChangeDate(LocalDateTime.now());
            //更新
            int i = teachplanMapper.updateById(teachplanDown);
            int i1 = teachplanMapper.updateById(teachplan);
            if (i <= 0 || i1 <= 0){
                XueChengPlusException.cast("信息更新失败");
            }
        }else {
            //如果当前教学计划在最后，那么不能进行下降
            LambdaQueryWrapper<Teachplan> last = new LambdaQueryWrapper<>();
            last.eq(Teachplan::getCourseId,teachplan.getCourseId());
            last.eq(Teachplan::getParentid,teachplan.getParentid());
            last.orderByAsc(Teachplan::getOrderby);
            List<Teachplan> teachplansOrderBy = teachplanMapper.selectList(last);
            Teachplan teachplan2 = teachplansOrderBy.get(teachplansOrderBy.size() - 1);
            if (teachplan2.getOrderby().equals(teachplan.getOrderby())){
                XueChengPlusException.cast("当前教学计划已在最下方");
            }
            //当前教学计划为小节，获取相同parentid的教学计划
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
            wrapper.eq(Teachplan::getParentid,teachplan.getParentid());
            wrapper.orderByAsc(Teachplan::getOrderby);
            //教学小节
            List<Teachplan> teachplans = teachplanMapper.selectList(wrapper);
            List<Teachplan> collect = teachplans.stream()
                    .filter(teachplan1 -> teachplan1.getOrderby() == teachplan.getOrderby() + 1)
                    .collect(Collectors.toList());
            System.out.println(collect);
            Teachplan teachplan1 = collect.get(0);
            //设置属性
            teachplan.setOrderby(teachplan.getOrderby() + 1);
            teachplan1.setOrderby(teachplan1.getOrderby() - 1);
            //进行更新
            int i = teachplanMapper.updateById(teachplan);
            int i1 = teachplanMapper.updateById(teachplan1);
            if (i <= 0 || i1 <= 0){
                XueChengPlusException.cast("信息更新失败");
            }
        }
    }

    @Override
    public void moveup(Long id) {
        //先查询，不为null再进行操作
        Teachplan teachplan = nullOfTeachPlanAndCourse(id);
        //判断当前教学计划为章节或者小节
        if (teachplan.getParentid().equals(0L)){
            //如果当前教学计划在最上，那么不能进行上升
            LambdaQueryWrapper<Teachplan> up = new LambdaQueryWrapper<>();
            up.eq(Teachplan::getCourseId,teachplan.getCourseId());
            up.orderByAsc(Teachplan::getOrderby);
            List<Teachplan> teachplansOrderBy = teachplanMapper.selectList(up);
            Teachplan teachplan2 = teachplansOrderBy.get(0);
            if (teachplan2.getOrderby().equals(teachplan.getOrderby())){
                XueChengPlusException.cast("当前教学计划已在最上方");
            }
            //当前教学计划为章节,获取全部章节
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
            wrapper.eq(Teachplan::getParentid,0L);
            List<Teachplan> teachplans = teachplanMapper.selectList(wrapper);
            List<Teachplan> collect = teachplans.stream()
                    .filter(teachplan1 -> teachplan1.getOrderby() == teachplan.getOrderby() - 1)
                    .collect(Collectors.toList());
            Teachplan teachplanUp = collect.get(0);
            //上面的元素下移
            teachplanUp.setOrderby(teachplanUp.getOrderby() + 1);
            //下面的元素上移
            teachplan.setOrderby(teachplan.getOrderby() - 1);
            teachplanUp.setChangeDate(LocalDateTime.now());
            teachplan.setChangeDate(LocalDateTime.now());
            //更新
            int i = teachplanMapper.updateById(teachplanUp);
            int i1 = teachplanMapper.updateById(teachplan);
            if (i <= 0 || i1 <= 0){
                XueChengPlusException.cast("信息更新失败");
            }
        }else {
            //如果当前教学计划在最后，那么不能进行下降
            LambdaQueryWrapper<Teachplan> last = new LambdaQueryWrapper<>();
            last.eq(Teachplan::getCourseId,teachplan.getCourseId());
            last.eq(Teachplan::getParentid,teachplan.getParentid());
            last.orderByAsc(Teachplan::getOrderby);
            List<Teachplan> teachplansOrderBy = teachplanMapper.selectList(last);
            //取出最上方的教学计划
            Teachplan teachplan2 = teachplansOrderBy.get(0);
            if (teachplan2.getOrderby().equals(teachplan.getOrderby())){
                XueChengPlusException.cast("当前教学计划已在最上方");
            }
            //当前教学计划为小节，获取相同parentid的教学计划
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
            wrapper.eq(Teachplan::getParentid,teachplan.getParentid());
            wrapper.orderByAsc(Teachplan::getOrderby);
            //教学小节
            List<Teachplan> teachplans = teachplanMapper.selectList(wrapper);
            List<Teachplan> collect = teachplans.stream()
                    .filter(teachplan1 -> teachplan1.getOrderby() == teachplan.getOrderby() - 1)
                    .collect(Collectors.toList());
            System.out.println(collect);
            //取出当前教学计划的上面的教学计划
            Teachplan teachplan1 = collect.get(0);
            //设置属性
            teachplan.setOrderby(teachplan.getOrderby() - 1);
            teachplan1.setOrderby(teachplan1.getOrderby() + 1);
            //进行更新
            int i = teachplanMapper.updateById(teachplan);
            int i1 = teachplanMapper.updateById(teachplan1);
            if (i <= 0 || i1 <= 0){
                XueChengPlusException.cast("信息更新失败");
            }
        }
    }

    private int getTeachplanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return  count+1;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId ==null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            //确定排序字段，找到它的同级节点个数，排序字段就是个数加1  select count(1) from teachplan where course_id=117 and parentid=268
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, parentid);
            teachplan.setOrderby(teachplanCount);
            teachplanMapper.insert(teachplan);

        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数复制到teachplan
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }


    /**
     * 课程计划绑定媒资
     * @param bindTeachplanMediaVO
     * @return
     */
    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaVO bindTeachplanMediaVO) {
        //先删除原有记录，根据课程计划id删除绑定的媒资
        Long teachplanId = bindTeachplanMediaVO.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
        if (teachplan == null){
            XueChengPlusException.cast("课程计划不存在");
        }
        if (teachplan.getGrade() != 2){
            XueChengPlusException.cast("只有在第二级教学计划中才可以插入视频");
        }
        //到这里才可以删除
        teachplanMediaMapper.delete(wrapper);
        //再添加新记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaVO,teachplanMedia);
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaVO.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    @Transactional
    public void deleteAssociationMedia(Long teachPlanId, String mediaId) {
        //先查询 再删除
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null){
            XueChengPlusException.cast("没有找到课程计划");
        }
        //校验mediaid和teachplanid是否一致
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getTeachplanId, teachPlanId)
        );
        System.out.println(mediaId + "  " + teachplanMedia.getMediaId());
        if (!teachplanMedia.getMediaId().equals(mediaId)){
            XueChengPlusException.cast("数据不一致");
        }
        //查询到了，删除计划关联的媒资文件
        int delete = teachplanMediaMapper.delete(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getTeachplanId, teachPlanId)
        );
        if (delete <= 0){
            XueChengPlusException.cast("删除失败");
        }
    }

    //根据传进来的教学计划id判断教学计划和课程是否存在
    //如果存在返回一个teachplan
    private Teachplan nullOfTeachPlanAndCourse(Long id){
        //先判断教学计划是否存在
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan != null){
            CourseBase courseBase = courseBaseMapper.selectById(teachplan.getCourseId());
            if (courseBase == null){
                //课程不存在就删除计划
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
                teachplanMapper.delete(wrapper);
                //再删除对应计划关联的media
                LambdaQueryWrapper<TeachplanMedia> Media = new LambdaQueryWrapper<>();
                Media.eq(TeachplanMedia::getCourseId,teachplan.getCourseId());
                XueChengPlusException.cast("当前教学计划对应的课程不存在");
            }
        }else {
            XueChengPlusException.cast("当前教学计划不存在");
        }
        return teachplan;
    }
}
