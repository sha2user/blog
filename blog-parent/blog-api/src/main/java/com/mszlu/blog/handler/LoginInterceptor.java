package com.mszlu.blog.handler;

import com.alibaba.fastjson.JSON;
import com.mszlu.blog.dao.pojo.SysUser;
import com.mszlu.blog.service.LoginService;
import com.mszlu.blog.utils.UserThreadLocal;
import com.mszlu.blog.vo.ErrorCode;
import com.mszlu.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    @Override
    //在执行Controller方法之前进行执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 1 判断请求的接口路径是否为HandlerMethod（Controller方法）
         * 2 判断token是否为空，如果为空，未登录；
         *   如果不为空，登录验证 loginService.checkToken
         * 3 如果认证成功 放行
         */
        //1
        if(!(handler instanceof HandlerMethod)){
            //handler可能是 RequestResourceHandler（SpringBoot程序访问静态资源，默认去classpath下的static目录去查询）
            return true;
        }
        //2
        String token = request.getHeader("Authorization");

        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");

        if(StringUtils.isBlank(token)){
            Result result=Result.fail(ErrorCode.NO_LOGIN.getCode(),"未登录");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        SysUser sysUser = loginService.checkToken(token);
        if(sysUser==null){
            Result result=Result.fail(ErrorCode.NO_LOGIN.getCode(),"未登录");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //3
        //如果希望在Controller中直接获取用户的信息，怎么获取呢？👇
        UserThreadLocal.put(sysUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,
                                Object handler,Exception ex)throws Exception{
        //如果不删除ThreadLocal中用完的信息，会有内存泄露的风险
        //☆☆☆☆☆☆☆☆让面试官眼前一亮的点☆☆☆☆☆☆☆☆☆☆☆☆☆
        UserThreadLocal.remove();
    }
}
