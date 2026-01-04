package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.dao.ProductDao;
import com.codearp.springboot.reactor.models.documents.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductRestController {

    private final ProductDao productDao;


    @GetMapping
    public Flux<Product> index() {
        return productDao.findAll()
                .map(prod -> {
                    prod.setName(prod.getName() != null ? prod.getName().toUpperCase() : null);
                    return prod;
                })
                .doOnNext(prod -> log.info(prod.getName()));
    }

    @GetMapping("/{id}")
    public Mono<Product> findById(@PathVariable("id") String id) {
        // Mono<Product> productMono = productDao.findById(id);
        return productDao.findAll().filter(p -> p.getId().equals(id))
                .next()
                .doOnNext(prod -> log.info(prod.getName()));
    }
}
