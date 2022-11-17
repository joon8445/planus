package com.planus.plan.controller;

import com.planus.db.entity.Timetable;
import com.planus.db.entity.Trip;
import com.planus.db.repository.TripRepository;
import com.planus.plan.dto.PlanIdResDTO;
import com.planus.plan.dto.PlanResDTO;
import com.planus.plan.dto.PlanSaveReqDTO;
import com.planus.plan.dto.TimetableListForMapResDTO;
import com.planus.plan.service.PlanService;
import com.planus.trip.service.TripService;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plans")
public class PlanController {
    @Autowired
    PlanService planService;
    @Autowired
    TripService tripService;

    @GetMapping("/{tripId}")
    @ApiOperation(value = "planId 목록 조회", notes = "tripId를 이용해 일정 정보를 불러오는 데 필요한 planId의 목록들을 받아옵니다.")
    public ResponseEntity getPlanId(@PathVariable long tripId) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            List<PlanIdResDTO> planIdResDTOList = planService.readPlanId(tripId);
            resultMap.put("planIdList", planIdResDTOList);
            resultMap.put("message", "success");
            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("message", "planId 조회 오류");
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/planlist")
    @ApiOperation(value = "planList 정보 조회", notes = "planId 목록을 이용해 일정 정보를 불러옵니다.")
    public ResponseEntity getPlanList(@RequestBody List<Long> planIdList) {

        Map<String, Object> resultMap = new HashMap<>();

        try {
            List<PlanResDTO> planResDTOList = planService.readPlanList(planIdList);
            resultMap.put("planList", planResDTOList);
            resultMap.put("message", "success");
            
            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("message", "planList 조회 오류");
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }

    //redis에 먼저 출발시간 변경사항을 저장한 후, 이 api를 호출해야 함
    //아마도 사용 안함 추후 삭제
    @GetMapping("/start/{planId}")
    @ApiOperation(value = "Plan 출발시간만 저장")
    public ResponseEntity savePlanStart(@PathVariable long planId) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
//            planService.savePlan(planId);
            resultMap.put("message", "success");
            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("message", "일정 출발 시간 저장 오류");
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping
    @ApiOperation(value = "Plan 및 Timetable 저장")
    public ResponseEntity savePlan(@RequestBody PlanSaveReqDTO planSaveReqDTO) {
        long tripId = planSaveReqDTO.getTripId();
        List<Long> planIdList = planSaveReqDTO.getPlanIdList();
        boolean complete = planSaveReqDTO.isComplete();

        Map<String, Object> resultMap = new HashMap<>();

        try {
            for (long planId:planIdList) {
                planService.savePlan(planId, complete);
                planService.saveTimetable(planId, complete);
            }

            //Trip의 complete를 변경
            tripService.changeComplete(tripId, complete);

            resultMap.put("message", "success");
            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("message", "일정 저장 오류");
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/map/{planId}")
    public ResponseEntity getTimetableListforMap(@PathVariable long planId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<TimetableListForMapResDTO> timetableList = planService.getTimetableListForMap(planId);

            resultMap.put("message", "success");
            resultMap.put("timetableList", timetableList);

            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("message", "타임테이블 가져오기 오류");
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }

    }

}
