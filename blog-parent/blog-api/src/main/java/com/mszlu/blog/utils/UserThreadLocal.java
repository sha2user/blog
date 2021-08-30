package com.mszlu.blog.utils;

import com.mszlu.blog.dao.pojo.SysUser;

public class UserThreadLocal {

    private UserThreadLocal(){}
    //ThreadLocal是进行线程变量隔离的，是线程安全问题里的一种方案
    private static final ThreadLocal<SysUser> LOCAL=new ThreadLocal<>();
    public static void put(SysUser sysUser){
        LOCAL.set(sysUser);
    }
    public static SysUser get(){
        return LOCAL.get();
    }

    public static void remove(){
        LOCAL.remove();
    }

}


