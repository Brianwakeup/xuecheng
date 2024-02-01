package com.xuecheng.base.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @Package:com.xuecheng.base.model
 * @Auther:Brianwei
 * @date:2024/1/21:0:12
 * @discribe: 分页查询结果模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PageResult<T> implements Serializable {

    //数据列表
    private List<T> items;

    //总记录数
    private Long counts;

    //当前页码
    private Long page;

    //每页记录数
    private Long pageSize;
}
