package com.planus.article.service;


import com.planus.article.dto.*;
import com.planus.db.entity.*;
import com.planus.db.repository.*;
import com.planus.exception.CustomException;
import com.planus.db.repository.ArticleRepository;
import com.planus.db.repository.TripRepository;
import com.planus.db.repository.UserRepository;
import com.planus.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final AreaRepository areaRepository;
    private final ArticleLikeRepository articleLikeRepository;

    @Override
    public ArticleResDTO findAllArticles(Pageable pageable) {
        Page<Article> articles = articleRepository.findAllByOrderByRegDateDesc(pageable);
        return ArticleResDTO.toDTO(articles);
    }

    @Override
    @Transactional
    public long createArticle(String token, ArticleReqDTO articleReqDTO) {
        Trip trip = tripRepository.findByTripId(articleReqDTO.getTripId());
        User user = userRepository.findByUserId(tokenProvider.getUserId(token.split(" ")[1]));
        Article article = ArticleReqDTO.toEntity(articleReqDTO, trip, user);
        articleRepository.save(article);
        System.out.println(tripRepository.findByTripId(articleReqDTO.getTripId()));
        return article.getArticleId();
    }

    @Override
    @Transactional
    public Map<String, String> deleteArticle(String token, long articleId) {
        HashMap<String, String> result = new HashMap<>();
        if (tokenProvider.getUserId(token.split(" ")[1])
                == articleRepository.findById(articleId).orElseThrow(() -> new CustomException("존재하지 않는 게시글입니다."))
                .getUser().getUserId()) {
            articleRepository.deleteById(articleId);
            result.put("data", articleId + "번 게시글이 삭제되었습니다.");
            return result;
        } else {
            return result;
        }
    }

    @Override
    @Transactional
    public long updateArticle(String token, ArticleReqDTO articleReqDTO, long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(() -> new CustomException("존재하지 않는 게시글입니다."));
        if (tokenProvider.getUserId(token.split(" ")[1]) == article.getUser().getUserId()) {
            article.updateArticle(articleReqDTO.getTitle(), articleReqDTO.getContent(), tripRepository.findByTripId(articleReqDTO.getTripId()), LocalDateTime.now());
            articleRepository.save(article);
        } else {
            System.out.println("실패!");
        }
        return articleId;
    }

    @Override
    public ArticleDetailResDTO findOneArticle(Long articleId) {
        // 조회수 올려주기!
        return null;
    }


    @Override
    public SearchResDTO getArticleListByTitle(String token, String title, Pageable pageable) {
        Page<Article> articleList = articleRepository.findByTitleContains(title, pageable);

        return SearchResDTO.builder()
                .currentPage(articleList.getNumber())
                .totalPage(articleList.getTotalPages())
                .articleList(setArticleList(token, articleList))
                .build();
    }

    @Override
    public SearchResDTO getArticleListByArea(String token, int[] area, Pageable pageable) {
        Page<Article> articleList = articleRepository.findByArea(area, pageable);

        return SearchResDTO.builder()
                .currentPage(articleList.getNumber())
                .totalPage(articleList.getTotalPages())
                .articleList(setArticleList(token, articleList))
                .build();
    }



    private List<SearchDTO> setArticleList(String token, Page<Article> articleList) {
        long userId = -1;
        if(token!=null){
            userId = tokenProvider.getUserId(token.split(" ")[1]);
        }

        List<SearchDTO> searchDTOList = new ArrayList<>();

        for (Article article : articleList){
            long articleId = article.getArticleId();
            long tripId = article.getTrip().getTripId();

            SearchDTO searchDTO = SearchDTO.builder()
                    .articleId(articleId)
                    .tripId(article.getTrip().getTripId())
                    .areaList(areaRepository.findAllByTripAreaList_Trip_TripId(tripId).stream().map(Area::getSiName).collect(Collectors.toList()))
                    .imageUrl(areaRepository.findTop1ByTripAreaList_Trip_TripId(tripId).getImageUrl())
                    .period(article.getTrip().getPeriod())
                    .userId(article.getUser().getUserId())
                    .name(article.getUser().getName())
                    .title(article.getTitle())
                    .regDate(article.getRegDate().toString())
                    .hits(article.getHits())
                    .likes(article.getArticleLikeList().size())
                    .isLiked(articleLikeRepository.existsByArticleArticleIdAndUserUserId(articleId,userId))
                    .build();

            searchDTOList.add(searchDTO);
        }

        return searchDTOList;
    }

    @Override
    @Transactional
    public long likeArticle(String token, long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(() -> new CustomException("존재하지 않는 게시글입니다."));
        long userId = tokenProvider.getUserId(token.split(" ")[1]);
        if (articleLikeRepository.existsByArticleArticleIdAndUserUserId(articleId, userId)) {
            articleLikeRepository.deleteByArticleArticleIdAndUserUserId(articleId, userId);


        } else {
            ArticleLike articleLike = ArticleLike.builder()
                    .article(article)
                    .user(userRepository.findByUserId(userId))
                    .build();
            articleLikeRepository.save(articleLike);
        }
        return articleLikeRepository.countByArticleArticleId(articleId);
    }


}
