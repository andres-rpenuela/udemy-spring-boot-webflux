package com.codearp.springboot.reactor.springbootsebfluxapirest.controllers;

import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException: {}", ex.getMessage(), ex);

        HttpStatusCode status = ex.getStatusCode();
        String reason;
        if (status instanceof HttpStatus hs) {
            reason = hs.getReasonPhrase();
        } else {
            reason = status.toString();
        }

        ErrorResponse body = new ErrorResponse();
        body.setStatus(status.value());
        body.setError(reason);
        body.setMessage(ex.getReason());
        return Mono.just(ResponseEntity.status(status).body(body));
    }


    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = new ErrorResponse();
        body.setStatus(status.value());
        body.setError(status.getReasonPhrase());
        body.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(status).body(body));
    }
}