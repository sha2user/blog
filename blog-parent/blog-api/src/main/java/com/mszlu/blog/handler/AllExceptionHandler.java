package com.mszlu.blog.handler;

import com.mszlu.blog.vo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice  //对加了@Ccontroller注解的方法进行拦截处理 （Advice AOP的实现）
public class AllExceptionHandler {
    //进行异常处理，处理Exception.class的异常
    @ExceptionHandler(Exception.class)
    @ResponseBody //返回json数据
    public Result doException(Exception ex){
        //到时加日志，记录到日志，现在仅返回到堆栈。
        ex.printStackTrace();
        return Result.fail(-999,"系统异常");
    }

}
