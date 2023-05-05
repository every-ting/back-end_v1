package com.ting.ting.controller;

import com.ting.ting.exception.TingApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(TingApplicationException.class)
    public ResponseEntity<?> tingExceptionHandler(TingApplicationException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().body("잘못된 정보를 입력하였습니다.");
    }
}
