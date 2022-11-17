package com.planus.config;

import com.planus.login.JwtAuthenticationEntryPoint;
import com.planus.login.filter.JwtFilter;
import com.planus.login.handler.OAuth2AuthenticationSuccessHandler;
import com.planus.login.service.UserOauth2Service;
import com.planus.util.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final TokenProvider tokenProvider;


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    //로컬
                    cors.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:8081", "https://planus.co.kr", "https://planus.co.kr/planus"));
                    cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
                    cors.setAllowedHeaders(Arrays.asList("token", "Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization", "refreshToken"));
                    cors.setExposedHeaders(Arrays.asList("token", "refresh"));
                    cors.setAllowCredentials(true);
                    return cors;
                })
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling()
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .and().authorizeRequests()
                //login
                .antMatchers(HttpMethod.PATCH, "/login").permitAll()
                .antMatchers("/login/**").hasAuthority("ROLE_MEMBER")
                //articles
                .antMatchers(HttpMethod.GET, "/articles/**").permitAll()
                .antMatchers("/articles/**").hasAuthority("ROLE_MEMBER")
                //buckets
                .antMatchers("/buckets/**").hasAuthority("ROLE_MEMBER")
                //comment
                .antMatchers(HttpMethod.GET, "/comment/**").permitAll()
                .antMatchers("/comment/**").hasAuthority("ROLE_MEMBER")
                //complete
                .antMatchers("/complete/copy/**").hasAuthority("ROLE_MEMBER")
                .antMatchers("/complete/**").permitAll()
                //mytrip
                .antMatchers("/mytrip/**").hasAuthority("ROLE_MEMBER")
                //plans
                .antMatchers("/plans/start/**").hasAuthority("ROLE_MEMBER")
                .antMatchers(HttpMethod.GET, "/plans/**").permitAll()
                .antMatchers("/plans/**").hasAuthority("ROLE_MEMBER")
                //recommend
                .antMatchers("/recommend/**").hasAuthority("ROLE_MEMBER")
                //trip
                .antMatchers(HttpMethod.GET, "/trip/member/**").permitAll()
                .antMatchers("/trip/member/**").hasAuthority("ROLE_MEMBER")
                .antMatchers("/trip/changeadmin").hasAuthority("ROLE_MEMBER")
                .antMatchers(HttpMethod.POST, "/trip").hasAuthority("ROLE_MEMBER")
                .antMatchers("/trip/**").permitAll()
                //mypage
                .antMatchers("/mypage/**").hasAuthority("ROLE_MEMBER")
                .and().oauth2Login()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .userInfoEndpoint().userService(userOauth2Service).and()
                .and().apply(new JwtSecurityConfig(tokenProvider))
                .and().build();
    }
}
