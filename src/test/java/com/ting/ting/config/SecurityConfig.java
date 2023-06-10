package com.ting.ting.config;

import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Import(SecurityConfig.class)
public class SecurityConfig {

    @BeforeTestMethod
    public void securitySetUp() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getName()).willReturn("1L"); // 원하는 userId 값을 반환하도록 설정
    }
}
