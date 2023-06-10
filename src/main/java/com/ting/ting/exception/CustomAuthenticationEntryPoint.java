package com.ting.ting.exception;

import com.ting.ting.dto.response.Response;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        TingApplicationException e = (TingApplicationException)request.getAttribute("exception");
        int status = ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus();
        Response<?> body = Response.error(ErrorCode.INTERNAL_SERVER_ERROR);

        if (e != null && e.getErrorCode() == ErrorCode.INVALID_ACCESS_TOKEN) {
            status = ErrorCode.INVALID_ACCESS_TOKEN.getHttpStatus();
            body = new Response<>(e);
        }

        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(body.toStream());
    }
}
