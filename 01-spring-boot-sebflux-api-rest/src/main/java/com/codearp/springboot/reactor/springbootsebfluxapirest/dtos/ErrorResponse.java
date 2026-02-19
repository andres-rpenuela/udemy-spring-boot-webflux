package com.codearp.springboot.reactor.springbootsebfluxapirest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse {

    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;

}