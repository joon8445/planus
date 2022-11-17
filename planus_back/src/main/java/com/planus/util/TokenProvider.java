package com.planus.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    //TODO: property로 빼기
    private static Long accessTokenTime = 1000*60*30L;
    private static Long refreshTokenTime = 2*60*60*24*30*1000L;

    public String createToken(Authentication authentication, long userId){
        String authorities = authentication
                .getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> kakaoAccount = ((OAuth2User) authentication.getPrincipal()).getAttribute("kakao_account");
        String email = (String) kakaoAccount.get("email");

        Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");
        String nickname = (String)profile.get("nickname");
        String imageUrl = (String)profile.get("profile_image_url");

        Date validity = getValidity(accessTokenTime);

        String token = Jwts.builder()
                .setSubject("planus_token")
                .claim("auth", authorities)
                .claim("userId", String.valueOf(userId))
                .claim("nickname", nickname)
                .claim("email", email)
                .claim("imageUrl", imageUrl)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .setExpiration(validity)
                .compact();
        return token;
    }

    public String createNewAccessToken(com.planus.db.entity.User user) {
        String token = Jwts.builder()
                .setSubject("planus_token")
                .claim("auth", "ROLE_MEMBER")
                .claim("userId", String.valueOf(user.getUserId()))
                .claim("nickname", user.getName())
                .claim("email", user.getEmail())
                .claim("imageUrl",user.getImageUrl())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .setExpiration(getValidity(accessTokenTime))
                .compact();
        return token;
    }

    public String createRefreshToken(){
        String token = Jwts.builder()
                .setSubject("planun_refresh_token")
                .setExpiration(getValidity(refreshTokenTime))
                .compact();
        return token;
    }

    private Date getValidity(Long tokenTime) {
        Long now = new Date().getTime();
        Date validity = new Date(now + tokenTime);
        return validity;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth")
                        .toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(),"",authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);

    }
    //TODO: 사용안되고 있는지 확인 후 삭제
//    public boolean validateToken(String token) throws Exception{
//        try{
//            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
//            return true;
//        }catch(SecurityException | MalformedJwtException | SignatureException e){
//            throw new SignatureException("JWT 서명 문제");
//        }catch(ExpiredJwtException e){
//            throw new ExpiredJwtException(null, null, "만료된 토큰문제");
//        }catch(UnsupportedJwtException e){
//            throw new UnsupportedJwtException("지원안되는 토큰");
//        }catch(IllegalArgumentException e){
//            throw new IllegalArgumentException();
//        }
//    }

    public long getUserId(String token){
        try{
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return Long.parseLong( String.valueOf(claims.get("userId")));
        }catch (SignatureException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public String updateTokenNickname(String token, String newNickname){
        Date validity = getValidity(accessTokenTime);
        Claims claims = Jwts
                .parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        String newToken = Jwts.builder()
                .setClaims(claims)
                .claim("nickname", newNickname)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .setExpiration(validity)
                .compact();

        return newToken;
    }
}
