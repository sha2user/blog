package com.mszlu.blog.service;


import com.mszlu.blog.vo.Result;

public interface CommentsService {

    /**
     * 根据文章ID查询所有评论列表
     * @param id
     * @return
     */
    Result commentsByArticleId(Long id);
}
