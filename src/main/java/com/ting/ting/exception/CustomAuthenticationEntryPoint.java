package com.ting.ting.exception;

import com.ting.ting.dto.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (response.getWriter() == null) {
            TingApplicationException e = new TingApplicationException(ErrorCode.UNAUTHORIZED, ServiceType.AUTHENTICATION, authException.getMessage());
            int status = e.getErrorCode().getHttpStatus();
            Response<?> body = new Response<>(e);

            log.error(e.getMessageForServer());
            response.setStatus(status);
            response.getWriter().write(body.toStream());
        }

        response.setContentType("application/json");
    }
}
