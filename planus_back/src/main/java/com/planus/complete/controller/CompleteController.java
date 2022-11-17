package com.planus.complete.controller;

import com.planus.complete.dto.CompleteReqDTO;
import com.planus.complete.dto.CompleteResDTO;
import com.planus.complete.service.CompleteService;
import com.planus.util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/complete")
@RequiredArgsConstructor
public class CompleteController {

    private final CompleteService completeService;
    private final FileUpload fileUpload;

    @GetMapping("/{uuid}")
    public ResponseEntity getCompleteTrip(@PathVariable(name = "uuid") String uuid) {
        Map<String, Object> resultMap = new HashMap();
        try {
            CompleteResDTO completeTrip = completeService.getCompleteTrip(uuid);

            resultMap.put("message", "success");
            resultMap.put("result", completeTrip);

            return new ResponseEntity(resultMap, HttpStatus.OK);
        } catch (Exception e) {
            resultMap.put("message", "완료여행 받기 실패");

            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/copy")
    public ResponseEntity<String> copyTrip(@RequestHeader String Authorization, @RequestBody CompleteReqDTO completeReqDTO) {
        return ResponseEntity.ok().body(completeService.copyTrip(Authorization, completeReqDTO.getTripId(), completeReqDTO.getStartDate()));
    }

    @PostMapping("/image")
    public ResponseEntity<String> completeImageUpload(@RequestPart Long tripId, @RequestPart MultipartFile file) throws IOException {
            return ResponseEntity.ok().body(completeService.completeImageUpload(tripId, file));
    }

    // S3 파일 삭제 용도(테스트)
    @DeleteMapping("/image")
    public ResponseEntity<String> completeImageUpload(String fileUrl) {
        fileUpload.fileDelete(fileUrl);
        return ResponseEntity.ok().body("success");
    }
}
