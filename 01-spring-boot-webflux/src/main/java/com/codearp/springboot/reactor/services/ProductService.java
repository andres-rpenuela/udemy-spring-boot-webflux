package com.codearp.springboot.reactor.services;

import com.codearp.springboot.reactor.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<Product> findAll();
    Flux<Product> findAllUpperCaseNames();
    Flux<Product> findAllUpperCaseNamesRepeat();

    Mono<Product> findById(String id);

    Mono<Product> save(Product product);
    Mono<Product> update(Product product, String id);

    Mono<Void> delete(String id);
}
