package com.ting.ting.exception;

import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.TingApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(TingApplicationException.class)
    public Response<?> tingExceptionHandler(TingApplicationException e) {
        log.error(e.getMessageForServer());
        return new Response<>(e);
    }

    @ExceptionHandler(RuntimeException.class)
    public Response<?> tingExceptionHandler(RuntimeException e) {
        log.error(e.getMessage());
        return Response.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
