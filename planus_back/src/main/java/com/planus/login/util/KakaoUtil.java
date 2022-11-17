package com.planus.login.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class KakaoUtil {

    @Value("${kakao.adminkey}")
    private String adminKey;

    public void kakaoSignOut(long kakaoId) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();
        String result="";
        URL url = new URL("https://kapi.kakao.com/v1/user/unlink");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try{

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization", "KakaoAK " + adminKey);
            conn.setDoOutput(true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("target_id_type=user_id");
            sb.append("&target_id="+kakaoId);

            bw.write(sb.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String temp="";
            while((temp=br.readLine())!=null){
                result+=temp;
            }
            JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
            br.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
