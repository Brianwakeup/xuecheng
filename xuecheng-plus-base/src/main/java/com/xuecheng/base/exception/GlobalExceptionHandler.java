package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;

/**
 * @Package:com.xuecheng.base.exception
 * @Auther:Brianwei
 * @date:2024/2/3:16:11
 * @discribe: 对抛出的异常进行统一处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 对项目的自定义异常进行处理
     * @param e
     * @return
     */
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e){
        //记录异常信息
        log.error("系统异常{}",e.getMessage(),e);
        //解析出异常信息
        String message = e.getMessage();
        RestErrorResponse restErrorResponse = new RestErrorResponse(message);
        return restErrorResponse;
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse Exception(Exception e){
        //记录异常信息
        log.error("系统异常{}",e.getMessage(),e);
        RestErrorResponse restErrorResponse = new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
        return restErrorResponse;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse Exception(MethodArgumentNotValidException e){
        //获取报出的错误,将错误放入集合中
        ArrayList<String> error = new ArrayList<>();
        e.getBindingResult().getFieldErrors().stream().forEach(item -> {
            error.add(item.getDefaultMessage());
        });
        //将错误拼接起来
        String errors = StringUtils.join(error, ",");
        //记录异常信息
        log.error("系统异常{}",e.getMessage(),e);
        RestErrorResponse restErrorResponse = new RestErrorResponse(errors);
        return restErrorResponse;
    }


}
