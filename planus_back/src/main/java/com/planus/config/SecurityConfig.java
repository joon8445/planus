package com.planus.config;

import com.planus.login.JwtAuthenticationEntryPoint;
import com.planus.login.handler.OAuth2AuthenticationSuccessHandler;
import com.planus.login.service.UserOauth2Service;
import com.planus.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final UserOauth2Service userOauth2Service;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final TokenProvider tokenProvider;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors().configurationSource(request -> {
            CorsConfiguration cors = new CorsConfiguration();
            //로컬
//            cors.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:8081"));
            cors.setAllowedOriginPatterns(Collections.singletonList("*"));
            cors.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE", "OPTIONS", "HEAD"));
//            cors.setAllowedMethods(Arrays.asList("*"));
            cors.setAllowedHeaders(Arrays.asList("token", "Origin","Accept","X-Requested-With","Content-Type","Access-Control-Request-Method","Access-Control-Request-Headers","Authorization"));
//            cors.setAllowedHeaders(Arrays.asList("*"));
            cors.setAllowCredentials(true);
            return cors;
        })
                .and()
                    .csrf().disable().headers().frameOptions().disable()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and().authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/login/test").hasAuthority("ROLE_MEMBER")
                .and().oauth2Login()
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .userInfoEndpoint().userService(userOauth2Service).and()
                .and().apply(new JwtSecurityConfig(tokenProvider))
                .and().build();

    }
}
