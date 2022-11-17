package com.planus.article.service;


import com.planus.article.dto.*;
import com.planus.db.entity.*;
import com.planus.db.repository.*;
import com.planus.exception.CustomException;
import com.planus.db.repository.ArticleRepository;
import com.planus.db.repository.TripRepository;
import com.planus.db.repository.UserRepository;
import com.planus.mytrip.dto.MyTripResDTO;
import com.planus.plan.dto.PlanResDTO;
import com.planus.plan.dto.TimetableListResDTO;
import com.planus.user.dto.UserResDTO;
import com.planus.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
        Page<Article> articles = articleRepository.findAllByOrderByArticleIdDesc(pageable);
        return ArticleResDTO.toDTO(articles);
    }

    @Override
    @Transactional
    public long createArticle(String token, ArticleReqDTO articleReqDTO) {
        Trip trip = tripRepository.findByTripId(articleReqDTO.getTripId());
        User user = userRepository.findByUserId(tokenProvider.getUserId(token.split(" ")[1]));
        Article article = ArticleReqDTO.toEntity(articleReqDTO, trip, user);
        articleRepository.save(article);
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
        }
        return articleId;
    }

    @Override
    @Transactional
    public ArticleDetailResDTO findOneArticle(long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(() -> new CustomException("존재하지 않는 게시글입니다."));
        article.addHits();
        int likeCount = articleLikeRepository.countByArticleArticleId(articleId);
        UserResDTO user = UserResDTO.toResDto(article.getUser());
        MyTripResDTO trip = MyTripResDTO.toResDTO(article.getTrip());
        List<Plan> plans = article.getTrip().getPlanList();
        List<PlanResDTO> planList = plans.stream().map(plan -> PlanResDTO.toResDTO(plan)).collect(Collectors.toList());
        return ArticleDetailResDTO.toEntity(article, likeCount, articleLikeRepository.existsByArticleArticleIdAndUserUserId(articleId, user.getUserId()), user, trip, planList);

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
    public List<SearchDTO> getArticleListByArea(String token, int[] area, Pageable pageable) {
        List<Article> articleList = articleRepository.findByArea(area, pageable);

        return setArticleList(token, articleList);
    }

    @Override
    public int countPage(int[] area){
        int count = articleRepository.countPage(area);
        int countPage = count/6;
        if(count%6!=0){
            countPage+=1;
        }
        return countPage;
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

    private List<SearchDTO> setArticleList(String token, List<Article> articleList) {
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

    @Override
    public ArticleResDTO getMyArticles(String token, Pageable pageable) {
        long userId = tokenProvider.getUserId(token.split(" ")[1]);
        Page<Article> articleList = articleRepository.findByUserUserIdOrderByArticleIdDesc(userId, pageable);
        return ArticleResDTO.toDTO(articleList);
    }

    @Override
    public ArticleResDTO getMyLikedArticles(String token, Pageable pageable) {
        long userId = tokenProvider.getUserId(token.split(" ")[1]);
        Page<Article> articles = articleRepository.findArticleByUserUserId(userId, pageable);
        return ArticleResDTO.toDTO(articles);

    }


}
