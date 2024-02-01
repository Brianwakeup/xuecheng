package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Package:com.xuecheng.base.model
 * @Auther:Brianwei
 * @date:2024/1/20:23:36
 * @discribe: 分页查询条件模型
 */

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class PageParams {

    //当前页码
    private Long pageNo;       //mybatisplus分页的类型就是long，方便接收

    //每页记录数默认值
    private Long pageSize;
}
