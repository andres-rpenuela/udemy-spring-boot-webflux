package com.codearp.springboot.reactor.springbootsebfluxapirest.facades;

import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShopFacade {

    Flux<ProductDto> recoverAllProducts();
    Mono<ProductDto> recoverProductById(String id);

    Mono<ProductDto> saveProduct(ProductDto productDto);

    Mono<Void> deleteProductById(String id);
}
