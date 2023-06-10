package com.ting.ting.configuration;

import com.ting.ting.configuration.filter.JwtTokenFilter;
import com.ting.ting.exception.CustomAuthenticationEntryPoint;
import com.ting.ting.service.UserService;
import com.ting.ting.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public SecurityConfig(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .mvcMatchers("/ting/**", "/kakao/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new JwtTokenFilter(userService, jwtTokenUtil), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                .build();
    }
}
