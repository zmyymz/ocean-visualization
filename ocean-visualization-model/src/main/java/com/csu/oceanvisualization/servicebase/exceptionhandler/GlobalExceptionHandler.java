package com.csu.oceanvisualization.servicebase.exceptionhandler;



import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.csu.oceanvisualization.commonutils.Result;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.error().message("执行了全局异常处理..");
    }


    //特定异常, 除0异常
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e) {
        e.printStackTrace();
        return Result.error().message("执行了ArithmeticException异常处理..");
    }

    //自定义异常
    @ExceptionHandler(OceanException.class)
    @ResponseBody
    public Result error(OceanException e) {
        log.error(e.getMessage());
        e.printStackTrace();
        return Result.error().code(e.getCode()).message(e.getMsg());
    }
}
