package com.heima.model.article.dtos;

import com.heima.model.article.pojos.ApArticle;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ArticleDto extends ApArticle {
    /**
     * 文章内容
     */
    private String content;
}
