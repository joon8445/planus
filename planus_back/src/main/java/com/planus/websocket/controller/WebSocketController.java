package com.planus.websocket.controller;

import com.planus.bucket.service.BucketService;
import com.planus.db.entity.Plan;
import com.planus.db.repository.PlanRepository;
import com.planus.db.entity.Trip;
import com.planus.db.repository.UserRepository;
import com.planus.plan.service.PlanService;
import com.planus.trip.service.MemberService;
import com.planus.trip.service.TripService;
import com.planus.util.TokenProvider;
import com.planus.websocket.model.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/ws")
@RequiredArgsConstructor
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final SimpMessageSendingOperations sendingOperations;
    private static final String ROOT_URL = "/topic/planus/";

    private final BucketService bucketService;
    private final PlanService planService;
    private final MemberService memberService;

    private final TripService tripService;

    private final PlanRepository planRepository;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    private static List<String> sessionUsers = Collections.synchronizedList(new ArrayList<>());

    @MessageMapping("/enter")
    public void enter(WebSocketMember member, SimpMessageHeaderAccessor headerAccessor){
        try{
            //퇴장이벤트 처리를 위해 token, tripId 웹소켓세션에 데이터 저장
            long tripId = member.getTripId();
            String token = member.getToken();

            //redis 접속자 목록에 추가
            memberService.addConnector(member);
            headerAccessor.getSessionAttributes().put("token",token);
            headerAccessor.getSessionAttributes().put("tripId",tripId);

            sessionUsers.add(userRepository.findByUserId(tokenProvider.getUserId(token.split(" ")[1])).getName());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @MessageMapping("/connector")
    public void getConnector(WebSocketMember member){
        member.setConnector(memberService.getConnector(member));
        member.setAction(0);
        try{
            sendingOperations.convertAndSend(ROOT_URL+member.getTripId(),member);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat")
    public void chat(WebSocketMessage message){
        message.setAction(1);
        sendingOperations.convertAndSend(ROOT_URL+message.getTripId(),message);
    }

    @MessageMapping("/addBucket")
    public void addBucket(WebSocketBucket bucket){
        bucket.setAction(2);
        bucketService.addBucket(bucket);
        sendingOperations.convertAndSend(ROOT_URL+bucket.getTripId(),bucket);
    }

    @MessageMapping("/delBucket")
    public void delBucket(WebSocketBucket bucket){
        bucket.setAction(3);
        bucketService.deleteBucket(bucket);
        sendingOperations.convertAndSend(ROOT_URL+bucket.getTripId(),bucket);
    }

    @MessageMapping("/setPlan")
    public void setPlan(WebSocketPlan plan){
        plan.setAction(4);
//        TODO: redis에 해당 Plan 수정
        planService.setPlan(plan.getPlanId(), plan);
        sendingOperations.convertAndSend(ROOT_URL+plan.getTripId(),plan);
    }

    @MessageMapping("/addTimetable")
    public void addTimetable(WebSocketTimetable timetable){
        timetable.setAction(5);
        if(timetable.getFromBucket()){
            // TODO: bucket redis 작업
            bucketService.moveToPlan(timetable);

        }
//        TODO: redis에 해당 timetable 추가하기
        planService.addTimetable(timetable.getPlanId(), timetable);
        sendingOperations.convertAndSend(ROOT_URL+timetable.getTripId(),timetable);
    }

    @MessageMapping("/delTimetable")
    public void delTimetable(WebSocketTimetableList timetablelist){
        timetablelist.setAction(6);
//        TODO: redis에 해당 timetable 삭제하기
        planService.delTimetable(timetablelist.getPlanId(), timetablelist);
        sendingOperations.convertAndSend(ROOT_URL+timetablelist.getTripId(),timetablelist);
    }

    @MessageMapping("/setTimetableOrders")
    public void setTimetableOrders(WebSocketTimetableList timetableList) {
        timetableList.setAction(7);
        planService.setTimetableOrders(timetableList.getPlanId(), timetableList);
        sendingOperations.convertAndSend(ROOT_URL+timetableList.getTripId(),timetableList);
    }

    @MessageMapping("/setTimetable")
    public void setTimetable(WebSocketTimetable Timetable) {
        Timetable.setAction(8);
        planService.setTimetable(Timetable.getPlanId(), Timetable);
        sendingOperations.convertAndSend(ROOT_URL+Timetable.getTripId(),Timetable);
    }


    @MessageMapping("/changeAdmin")
    public void changeAdmin(WebSocketAdmin admin){
        admin.setAction(9);
        long originAdminId = admin.getOriginAdminId();
        Trip trip = tripService.findByTripId(admin.getTripId());
        if(trip.getAdmin()!=originAdminId){
            return;
        }else if (!memberService.isMemberOfTrip(admin.getTripId(), admin.getNewAdminId())){
            return;
        }else{
            tripService.changeAdminForWebSocket(trip,admin.getNewAdminId());
            sendingOperations.convertAndSend(ROOT_URL+admin.getTripId(), admin);
        }
    }

    @MessageMapping("/completeTrip")
    public void completeTrip(WebSocketComplete complete) {
        complete.setAction(10);
        sendingOperations.convertAndSend(ROOT_URL+complete.getTripId(),complete);
    }

    @EventListener
    public void exit(SessionDisconnectEvent event) {
        //연결된 모든 클라이언트에게 사용자 퇴장 이벤트 처리
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        try {
            WebSocketMember member = new WebSocketMember();

            //웹소켓세션에서 token과 tripId 가져오기
            member.setTripId((long)headerAccessor.getSessionAttributes().get("tripId"));
            member.setToken(headerAccessor.getSessionAttributes().get("token").toString());

            //redis 접속자 목록에서 삭제
            memberService.delConnector(member);

            String nickname = userRepository.findByUserId(tokenProvider.getUserId(member.getToken().split(" ")[1])).getName();
            sessionUsers.remove(nickname);


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sessionUsers.size() == 0) {
            // 웹소켓에서 한 명이 퇴장할 때마다 redis에 있는 값을 mysql에 저장
            List<Plan> planIdList = planRepository.findByTripTripId((long)headerAccessor.getSessionAttributes().get("tripId")).orElseThrow();
            for (Plan p:planIdList) {
                planService.savePlan(p.getPlanId(), false);
                planService.saveTimetable(p.getPlanId(), false);
            }
            bucketService.createBucketList((long)headerAccessor.getSessionAttributes().get("tripId"));
        }




        //웹소켓세션 비우기
        headerAccessor.getSessionAttributes().clear();
    }
}
