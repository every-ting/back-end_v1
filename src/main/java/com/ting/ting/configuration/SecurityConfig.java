package com.ting.ting.configuration;

import com.ting.ting.util.JwtTokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public JwtTokenGenerator JwtTokenGenerator() {
        return new JwtTokenGenerator(secret);
    }
}
