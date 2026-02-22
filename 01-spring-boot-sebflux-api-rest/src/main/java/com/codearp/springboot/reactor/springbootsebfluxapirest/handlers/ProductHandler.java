package com.codearp.springboot.reactor.springbootsebfluxapirest.handlers;

import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductService productService;

    public Mono<ServerResponse> listPeople(ServerRequest request) {
        return ServerResponse.ok()
                .body(productService.recoverALlProduct(), ProductDto.class);
    }

}
