package com.planus.login.controller;

import com.planus.login.util.KakaoUtil;
import com.planus.user.dto.UserInfoResDTO;
import com.planus.user.service.UserService;
import com.planus.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    private final TokenProvider tokenProvider;

    private final KakaoUtil kakaoUtil;


    @GetMapping("/signout")
    public void signOut(@RequestHeader String Authorization, HttpServletResponse response) throws IOException {
        String token = Authorization.split(" ")[1];
        long userIdFromToken = tokenProvider.getUserId(token);
        long kakaoId = userService.findKakaoIdByUserId(userIdFromToken);
        try {
            userService.changeAdminForSignOut(userIdFromToken);
            kakaoUtil.kakaoSignOut(kakaoId);
            userService.deleteUser(userIdFromToken);
        }catch(Exception e){
            e.printStackTrace();
        }
        return;
    }

    @PatchMapping
    public ResponseEntity refresh(@RequestBody Map<String, Object> data, HttpServletResponse response) throws IOException {
        Map<String, String> resultMap = new HashMap<>();
        String refreshToken = (String) data.get("refreshToken");
        String newAccessToken = userService.makeNewAccessToken(refreshToken);
        if(newAccessToken!=null){
            resultMap.put("newToken", newAccessToken);
            return new ResponseEntity(resultMap,HttpStatus.OK);
        }else{
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @GetMapping("/logout")
    public ResponseEntity logout(@RequestHeader String Authorization){
        Map<String, String> resultMap = new HashMap<>();
        String token = Authorization.split(" ")[1];
        try{
            userService.logout(token);
            resultMap.put("message", "success");
            return new ResponseEntity(resultMap, HttpStatus.OK);
        }catch(Exception e) {
            resultMap.put("message", "로그아웃에서 문제 발생");
            return new ResponseEntity(resultMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/dummy")
    public ResponseEntity dummy(){
        return new ResponseEntity(HttpStatus.OK);
    }
}
