package com.xuecheng.base.exception;

/**
 * @Package:com.xuecheng.base.exception
 * @Auther:Brianwei
 * @date:2024/2/3:15:57
 * @discribe: 项目自定义异常类型
 */
public class XueChengPlusException extends RuntimeException{

    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(String message){
        throw new XueChengPlusException(message);
    }
}
