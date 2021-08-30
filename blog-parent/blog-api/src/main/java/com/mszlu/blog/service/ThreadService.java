package com.mszlu.blog.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mszlu.blog.dao.mapper.ArticleMapper;
import com.mszlu.blog.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {

    //期望此操作在线程池执行，不会影响原有的主线程
    @Async("taskExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {

        int viewCounts=article.getViewCounts();
        Article articleUpdate = new Article();
        //  update方法所需参数：
        //  int update(@Param("et") T entity, @Param("ew") Wrapper<T> updateWrapper);
        articleUpdate.setViewCounts((viewCounts+1));
        LambdaUpdateWrapper<Article> updateWrapper= new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        //为了在多线程环境下的线程安全 要设置如下语句
        updateWrapper.eq(Article::getViewCounts,viewCounts);
        //👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆
        //sql语句： UPDATE article SET view_count=100 WHERE view_count=99 and id=xx
        articleMapper.update(articleUpdate,updateWrapper);
//        try {
//            Thread.sleep(4000);
//            System.out.println("更新完成了...");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
