package com.mszlu.blog.controller;

import com.mszlu.blog.service.LoginService;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("login")
public class LoginController {
    @Autowired
    private LoginService loginService;


    @PostMapping
    public Result login(@RequestBody LoginParam loginParam){
       //登录：验证用户，要访问用户表，做一个登录service
        return loginService.login(loginParam);
    }
}
