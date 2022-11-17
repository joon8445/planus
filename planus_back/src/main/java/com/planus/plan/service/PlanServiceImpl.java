package com.planus.plan.service;

import com.planus.db.entity.Plan;
import com.planus.db.entity.Timetable;
import com.planus.db.entity.Transit;
import com.planus.db.repository.PlanRepository;
import com.planus.db.repository.TimetableRepository;
import com.planus.db.repository.TripRepository;
import com.planus.plan.dto.*;
import com.planus.util.RedisUtil;
import com.planus.websocket.model.WebSocketPlan;
import com.planus.websocket.model.WebSocketTimetable;
import com.planus.websocket.model.WebSocketTimetableItem;
import com.planus.websocket.model.WebSocketTimetableList;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;
    private final TimetableRepository timetableRepository;
    private final RedisUtil redisUtil;

    // redis에 plan 정보가 있으면 redis, 없으면 mysql에서 plan 읽어오기
    @Override
    public List<PlanIdResDTO> readPlanId(long tripId) {
        List<PlanIdResDTO> planIdResDTOList = new ArrayList<>();

        List<Plan> planList = planRepository.findByTripTripId(tripId).orElseThrow();
        for (Plan p : planList) {
            PlanIdResDTO planIdResDTO = PlanIdResDTO.builder()
                    .planId(p.getPlanId())
                    .tripDate(p.getTripDate())
                    .build();
            planIdResDTOList.add(planIdResDTO);
        }

        return planIdResDTOList;
    }

    @Override
    public List<PlanResDTO> readPlanList(List<Long> planIdList) {
        List<PlanResDTO> planResDTOList = new ArrayList<>();

        // 각 plan별로 for문을 돌면서 redis에 있는지 없는지를 검사함
        for (long planId : planIdList) {
//            HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
            String planKey = "planList::" + planId;
            String timetableKey = "timetableList::" + planId;

            // redis에 값이 있으면 redis에서 가져오고,
            if (redisUtil.isExists(planKey)) {

                //planList::planId 를 key로 가진 hash에서 startTime, tripDate를 가져올 수 있음
                Map<String, String> map = redisUtil.getHashData(planKey);
                // 이 때 mysql의 Timetable 테이블에 해당 planId의 정보가 없으면 아예 redis에 timetableList::planId가 안 만들어짐
                List list = redisUtil.getListData(timetableKey);

                // "timetableList::(planId)" : "{"orders":"1","place":"서울","transit":"CAR"}", "{"orders":"2","place":"부산","transit":"CAR"}"
                try {
                    List<TimetableListResDTO> timetableListResDTOList = new ArrayList<>();
                    JSONParser parser = new JSONParser();
                    for (int i = 0; i < list.size(); i++) {
                        JSONObject jsonObject = (JSONObject) parser.parse((String) list.get(i));

                        TimetableListResDTO timetableListResDTO = TimetableListResDTO.builder()
//                                .timetableId()
                                .orders((int) (long) jsonObject.get("orders"))
                                .place(jsonObject.get("place").toString())
                                .lat((double) jsonObject.get("lat"))
                                .lng((double) jsonObject.get("lng"))
                                .transit(Transit.valueOf(jsonObject.get("transit").toString()))
                                .costTime((int) (long) jsonObject.get("cost_time"))
                                .moveTime((int) (long) jsonObject.get("move_time"))
                                .moveRoute(jsonObject.get("move_route").toString())
                                .build();

                        timetableListResDTOList.add(timetableListResDTO);
                    }

                    PlanResDTO planResDTO = PlanResDTO.builder()
                            .planId(planId)
                            .tripDate(LocalDate.parse(map.get("trip_date")))
                            .startTime(Integer.valueOf(map.get("start_time")))
                            .timetableList(timetableListResDTOList)
                            .build();

                    planResDTOList.add(planResDTO);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }

            // 없으면 mysql에서 가져오기 + redis에 저장하기
            else {

                //redis에 timetableList::planId가 있으면 삭제 후 넣기
                if(redisUtil.isExists(timetableKey)) {
                    redisUtil.deleteData(timetableKey);
                }

                Plan plan = planRepository.findByPlanId(planId).orElseThrow();
                List<Timetable> timetableList = timetableRepository.findByPlanPlanId(planId).orElseThrow();
                List<TimetableListResDTO> timetableListResDTOList = new ArrayList<>();

                for (Timetable t : timetableList) {
                    //timetable Builder (프론트로 보낼 값 저장)
                    TimetableListResDTO timetableListResDTO = TimetableListResDTO.builder()
                            .timetableId(t.getTimetableId())
                            .orders(t.getOrders())
                            .place(t.getPlace())
                            .lat(t.getLat())
                            .lng(t.getLng())
                            .costTime(t.getCostTime())
                            .transit(t.getTransit())
                            .moveTime(t.getMoveTime())
                            .moveRoute(t.getMoveRoute())
                            .build();

                    timetableListResDTOList.add(timetableListResDTO);

                    //redis에 저장하기(Timetable)
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("orders", t.getOrders());
                    jsonObject.put("place", t.getPlace());
                    jsonObject.put("lat", t.getLat());
                    jsonObject.put("lng", t.getLng());
                    jsonObject.put("cost_time", t.getCostTime());
                    jsonObject.put("move_time", t.getMoveTime());
                    jsonObject.put("transit", t.getTransit().toString());
                    jsonObject.put("move_route", t.getMoveRoute());

                    String timetableStr = jsonObject.toJSONString();

                    redisUtil.addListData(timetableKey, timetableStr);
                }

                //plan Builder
                PlanResDTO planResDTO = PlanResDTO.builder()
                        .planId(plan.getPlanId())
                        .tripDate(plan.getTripDate())
                        .startTime(plan.getStartTime())
                        .timetableList(timetableListResDTOList)
                        .build();

                planResDTOList.add(planResDTO);

                //redis에 저장하기 (Plan)
                Map<String, Object> map = new HashMap<>();

                map.put("start_time", String.valueOf(plan.getStartTime()));
                map.put("trip_date", String.valueOf(plan.getTripDate()));

                redisUtil.setHashData(planKey, map);
            }
        }

        return planResDTOList;
    }

    @Override
//    @CacheEvict(cacheNames = "planList", key = "#planId")
    public void savePlan(long planId, boolean complete) {
//        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String key = "planList::" + planId;

        if(!redisUtil.isExists(key)){
            return;
        }
        else {
        }

        Map<String, String> map = redisUtil.getHashData(key);

        Plan plan = Plan.builder()
                .planId(planId)
                .tripDate(LocalDate.parse(map.get("trip_date")))
                .startTime(Integer.valueOf(map.get("start_time")))
                .trip(planRepository.findByPlanId(planId).orElseThrow().getTrip())
                .build();

        planRepository.save(plan);

        //complete가 false면 redis 삭제 안 하고, true면 redis에서 삭제함(일정 완료)
        if(complete) {
            redisUtil.deleteData(key);
        }
    }

    //redis에서 여행정보를 가져와서 mysql에 저장
    @Override
    @Transactional
//    @CacheEvict(cacheNames = "timetableList", key = "#planId")
    public void saveTimetable(long planId, boolean complete) {
        //일별일정 수정(저장)
        //기존의 timetable 삭제
        //새로운 timetable로 저장
        //새로운 timetable과 redis에서 가져온 map 정보들로 plan 객체 만들어서 저장

        String key = "timetableList::" + planId;


        //사용자가 화면에서 timetable을 다 지워버려서 없는 경우와,
        //사용자가 아예 그 탭을 눌러보지도 않아서 없는 경우가 있음.
        if(!redisUtil.isExists(key)){
            return;
        }

        List list = redisUtil.getListData(key);

        //mysql에 저장되어 있는 timetable을 전부 삭제
        timetableRepository.deleteByPlanPlanId(planId);

        try {
            JSONParser parser = new JSONParser();
//            String jsonString = (String) map.get("timetables");
//            JSONObject jsonObj = (JSONObject) parser.parse(jsonString);
//            JSONArray timetables = (JSONArray) jsonObj.get("timetables");

            // redis에 저장되어있던 Timetable을 꺼내와서 mysql에 저장
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObject = (JSONObject) parser.parse((String) list.get(i));
                Timetable timetable = Timetable.builder()
                        .orders((int) (long) jsonObject.get("orders"))
                        .place(jsonObject.get("place").toString())
                        .lat((double) jsonObject.get("lat"))
                        .lng((double) jsonObject.get("lng"))
                        .transit(Transit.valueOf(jsonObject.get("transit").toString()))
                        .costTime((int) (long) jsonObject.get("cost_time"))
                        .moveTime((int) (long) jsonObject.get("move_time"))
                        .moveRoute(jsonObject.get("move_route").toString())
                        .plan(planRepository.findByPlanId(planId).orElseThrow())
                        .build();

                timetableRepository.save(timetable);
            }

            //complete가 false면 redis 삭제 안 하고, true면 redis에서 삭제함(일정 완료)
            if(complete) {
                redisUtil.deleteData(key);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<TimetableListForMapResDTO> getTimetableListForMap(long planId) {
        List<Timetable> list = timetableRepository.findByPlanPlanId(planId).orElseThrow();
        List<TimetableListForMapResDTO> timetableList = new ArrayList<>();
        for (Timetable t:list) {
            TimetableListForMapResDTO timetableListForMapResDTO = TimetableListForMapResDTO.builder()
                    .timetableId(t.getTimetableId())
                    .orders(t.getOrders())
                    .place(t.getPlace())
                    .lat(t.getLat())
                    .lng(t.getLng())
                    .planId(t.getPlan().getPlanId())
                    .build();

            timetableList.add(timetableListForMapResDTO);
        }

        return timetableList;
    }

    //웹소켓 요청 처리

    @Override
    public void setPlan(long planId, WebSocketPlan plan) {
        String key = "planList::" + planId;

        Map<String, Object> map = new HashMap<>();

        map.put("start_time", String.valueOf(plan.getStartTime()));
        map.put("trip_date", String.valueOf(plan.getTripDate()));

        redisUtil.setHashData(key, map);
    }

    @Override
    public void addTimetable(long planId, WebSocketTimetable timetable) {
        String key = "timetableList::" + planId;

        //여기서 새로 추가할 Timetable의 orders를 계산
        int orders = (int) redisUtil.getListDataSize(key)+1;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orders", orders);
        jsonObject.put("place", timetable.getPlace());
        jsonObject.put("lat", timetable.getLat());
        jsonObject.put("lng", timetable.getLng());
        jsonObject.put("cost_time", timetable.getCostTime());
        jsonObject.put("move_time", timetable.getMoveTime());
        jsonObject.put("transit", timetable.getTransit());
        jsonObject.put("move_route", timetable.getMoveRoute());

        String timetableStr = jsonObject.toJSONString();

        redisUtil.addListData(key, timetableStr);
    }

    @Override
    public void setTimetableOrders(long planId, WebSocketTimetableList timetableList) {
        String key = "timetableList::" + planId;

        //삭제
        redisUtil.deleteData(key);

        //새로 추가
        for (WebSocketTimetableItem item:timetableList.getTimetableList()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orders", item.getOrders());
            jsonObject.put("place", item.getPlace());
            jsonObject.put("lat", item.getLat());
            jsonObject.put("lng", item.getLng());
            jsonObject.put("cost_time", item.getCostTime());
            jsonObject.put("move_time", item.getMoveTime());
            jsonObject.put("transit", item.getTransit());
            jsonObject.put("move_route", item.getMoveRoute());

            String timetableStr = jsonObject.toJSONString();

            redisUtil.addListData(key, timetableStr);
        }
    }

    @Override
    public void setTimetable(long planId, WebSocketTimetable timetable) {
        String key = "timetableList::" + planId;

        //timetableList를 돌면서 파싱하고, orders가 일치하는 것을 찾아 다시 세팅하여 저장
        int order = timetable.getOrders();
        List<String> timetableListData = redisUtil.getListData(key);
        for (int i = 0; i < timetableListData.size(); i++) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(timetableListData.get(i));
                if((int) (long) jsonObject.get("orders") == order) {
                    JSONObject timetableObject = new JSONObject();

                    timetableObject.put("orders", timetable.getOrders());
                    timetableObject.put("place", timetable.getPlace());
                    timetableObject.put("lat", timetable.getLat());
                    timetableObject.put("lng", timetable.getLng());
                    timetableObject.put("cost_time", timetable.getCostTime());
                    timetableObject.put("move_time", timetable.getMoveTime());
                    timetableObject.put("transit", timetable.getTransit());
                    timetableObject.put("move_route", timetable.getMoveRoute());

                    String timetableStr = timetableObject.toJSONString();

                    redisUtil.setListData(key, i, timetableStr);

                    break;
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @Transactional
    public void delTimetable(long planId, WebSocketTimetableList timetableList) {
        String key = "timetableList::" + planId;
        //redis에서 timetableList::planId를 찾고, 전부 삭제한 후 새 timetableList 넣기
        if(redisUtil.isExists(key)){
            redisUtil.deleteData(key);
        }

        for (WebSocketTimetableItem item:timetableList.getTimetableList()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("orders", item.getOrders());
            jsonObject.put("place", item.getPlace());
            jsonObject.put("lat", item.getLat());
            jsonObject.put("lng", item.getLng());
            jsonObject.put("cost_time", item.getCostTime());
            jsonObject.put("move_time", item.getMoveTime());
            jsonObject.put("transit", item.getTransit());
            jsonObject.put("move_route", item.getMoveRoute());

            String timetableStr = jsonObject.toJSONString();

            redisUtil.addListData(key, timetableStr);
        }

        if(!redisUtil.isExists(key)){
            // 타임테이블을 모두 삭제하면 redis에서 timetableList도 삭제되어 이후 saveTimetable할 때 정상적으로 반영이 안 되므로 여기서 mysql 저장
            timetableRepository.deleteByPlanPlanId(planId);
        };
    }
}
