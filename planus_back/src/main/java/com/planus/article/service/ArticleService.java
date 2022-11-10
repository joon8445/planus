package com.planus.article.service;

import com.planus.article.dto.*;
import org.springframework.data.domain.Pageable;
import com.planus.db.entity.Article;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ArticleService {

    ArticleResDTO findAllArticles(Pageable pageable);

    long createArticle(String token, ArticleReqDTO articleReqDTO);

    //ArticleResDTO findOneArticle(long articleId);

    Map<String, String> deleteArticle(String token, long articleId);

    // article상세 조회 끝나고 returnType 변경
    long updateArticle(String token, ArticleReqDTO articleReqDTO, long articleId);

    ArticleDetailResDTO findOneArticle(Long articleId);

    SearchResDTO getArticleListByTitle(String token, String title, Pageable pageable);
    SearchResDTO getArticleListByArea(String token, int[] area, Pageable pageable);

    long likeArticle(String token, long articleId);
}
