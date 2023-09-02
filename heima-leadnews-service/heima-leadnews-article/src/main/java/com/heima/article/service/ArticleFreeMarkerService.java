package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreeMarkerService {

    /**
     * 生成静态文件上传到minIO中
     * @param apArticle
     * @param content
     */
    void buildArticleToMinIO(ApArticle apArticle,String content);
}
