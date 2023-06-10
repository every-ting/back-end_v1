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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

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
                        .mvcMatchers("/ting/**", "/kakao/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new CorsFilter(this.corsConfigurationSource()))
                .addFilterBefore(new JwtTokenFilter(userService, jwtTokenUtil), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD","POST","GET","DELETE","PUT"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
