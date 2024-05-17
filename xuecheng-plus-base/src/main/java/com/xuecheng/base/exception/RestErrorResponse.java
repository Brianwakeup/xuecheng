package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Package:com.xuecheng.base.exception
 * @Auther:Brianwei
 * @date:2024/2/3:15:55
 * @discribe: 和前端约定返回的异常信息模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestErrorResponse implements Serializable {

    private String errMessage;

}
