package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.RequestBody;

public interface ApArticleService extends IService<ApArticle> {
    /**
     * 加载文章列表
     * @param dto 封装的文章查询参数
     * @param type 1.加载更多 2.加载最新
     * @return
     */
    ResponseResult load(ArticleHomeDto dto,Short type);

    /**
     * 保存app端相关文章
     * @param dto
     * @return
     */
    ResponseResult saveArticle(ArticleDto dto);
}
