package com.planus.recommend.controller;

import com.planus.recommend.service.RecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    private final RecommendService recommendService;

    @Autowired
    public RecommendController(RecommendService recommendService){
        this.recommendService = recommendService;
    }

    @GetMapping
    public ResponseEntity getRecommends(@RequestParam double lat, @RequestParam double lng, Pageable pageable){
        Map<String, Object> resultMap = new HashMap<>();
        try{
            resultMap.put("recommendList", recommendService.findRecommendByLocation(lat,lng,pageable));
            return new ResponseEntity(resultMap,HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/length")
    public ResponseEntity getPageLength(@RequestParam double lat, @RequestParam double lng, @RequestParam int size){
        Map<String, Object> resultMap = new HashMap<>();
        if(size == 0){
            return new ResponseEntity(resultMap,HttpStatus.BAD_REQUEST);
        }
        try{
            resultMap.put("pageLength", recommendService.countPage(lat,lng,size));
            return new ResponseEntity(resultMap,HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }
}
