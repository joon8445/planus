package com.planus.login.handler;

import com.planus.db.entity.User;
import com.planus.user.service.UserService;
import com.planus.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final UserService userService;

    @Value("${base.url}")
    private String BASE_URL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        long kakaoId = getKakaoId(authentication);
        User user = userService.findUserByKakaoId(kakaoId);
        long userId = user.getUserId();
        String jwtToken = tokenProvider.createToken(authentication, userId);
        String refreshToken = user.getRefreshToken();
        if (response.isCommitted()) {
            return;
        }
        getRedirectStrategy().sendRedirect(request, response, makeRedirectUrl(jwtToken, refreshToken));
    }

    private long getKakaoId(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        long kakaoId = (long) attributes.get("id");
        return kakaoId;
    }

    private String makeRedirectUrl(String token, String refreshToken) {
        return UriComponentsBuilder.fromUriString(BASE_URL + "/login/getkakaotoken?token=" + token + "&refresh=" + refreshToken).build().toUriString();
    }
}
