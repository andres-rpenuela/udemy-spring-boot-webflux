package com.codearp.springboot.reactor.springbootsebfluxapirest.services;

import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<ProductDto> recoverALlProduct();
    Mono<ProductDto> recoverProductById(String id);

    Mono<ProductDto> saveProduct(ProductDto productDto);
    Mono<Void> deleteProductById(String id);
}
