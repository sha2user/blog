package com.mszlu.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.blog.dao.dos.Archives;
import com.mszlu.blog.dao.mapper.ArticleBodyMapper;
import com.mszlu.blog.dao.mapper.ArticleMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mszlu.blog.dao.pojo.Article;
import com.mszlu.blog.dao.pojo.ArticleBody;
import com.mszlu.blog.service.*;
import com.mszlu.blog.vo.ArticleBodyVo;
import com.mszlu.blog.vo.ArticleVo;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ThreadService threadService;

    @Override
    public Result listArticle(PageParams pageParams){
        /**
         * 1.分页查询article数据库表
         */
        Page page=new Page<>(pageParams.getPage(),pageParams.getPageSize());
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        //是否置顶排序 与 order by create_date desc
        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
        Page articlePage = articleMapper.selectPage(page, queryWrapper);
        List<Article> records=articlePage.getRecords();
        //不能直接返回,需要将Ariticle类型转换为ArticleVo类型并通过Result返回。
        List<ArticleVo> articleVoList=copyList(records,true,true);
        return Result.success(articleVoList);
    }

    /**
     * 最热文章
     * @param limit
     * @return
     */
    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by view_counts desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    //最新文章
    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by create_date desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }
    //文章归档
    @Override
    public Result listArchives() {
        List<Archives> archivesList=articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Override
    public Result findArticleById(Long articleId) {

        /**
         * 1 根据id查询文章信息
         * 2 根据bodyId和categoryId去做关联查询
         */
        Article article=this.articleMapper.selectById(articleId);
        ArticleVo articleVo=copy(article,true,true,true,true);
        //查看完文章，新增阅读数还未增加
        //看完文章后，本该直接返回数据，这时做了更新操作，更新时加锁，阻塞其他的读操作，性能比较低
        //更新增加了此次接口的耗时，如果一旦更新出问题，不能影响查看文章的操作
        //使用线程池解决 可以把更新操作放到线程池中执行，和主线程就不相关了
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }
    //==================================copyList方法为方法重载===================================
    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor){
        List<ArticleVo> articleVoList=new ArrayList<>();
        for(Article record:records){
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
        }
    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory){
        List<ArticleVo> articleVoList=new ArrayList<>();
        for(Article record:records){
            articleVoList.add(copy(record,isTag,isAuthor,true,true));
        }
        return articleVoList;
    }
    //======================================================================================

        private ArticleVo copy(Article article,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory){
            ArticleVo articleVo=new ArticleVo();
            articleVo.setId(String.valueOf(article.getId()));
            BeanUtils.copyProperties(article,articleVo);
            articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
            //并不是所有的接口都需要标签，作者信息
            if(isTag){
                Long articleId=article.getId();
                articleVo.setTags(tagService.findTagsByArticleId(articleId));
            }
            if(isAuthor){
                Long authorId = article.getAuthorId();
                articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
            }
            if(isBody){
                Long bodyId = article.getBodyId();
                articleVo.setBody(findArticleBodyById(bodyId));
            }
            if(isCategory){
                Long categoryId = article.getCategoryId();
                articleVo.setCategory(categoryService.findCategoryById(categoryId));
            }
            return articleVo;
        }

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo=new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
