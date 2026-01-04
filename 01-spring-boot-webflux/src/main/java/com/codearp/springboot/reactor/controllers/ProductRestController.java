package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.models.documents.Product;
import com.codearp.springboot.reactor.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductRestController {

    private final ProductService productService;

    @GetMapping
    public Flux<Product> index() {
        /*return productDao.findAll()
                .map(prod -> {
                    prod.setName(prod.getName() != null ? prod.getName().toUpperCase() : null);
                    return prod;
                })
                .doOnNext(prod -> log.info(prod.getName()));*/
        return productService.findAllUpperCaseNamesRepeat()
                .doOnNext(prod -> log.info(prod.getName()));
    }

    @GetMapping("/{id}")
    public Mono<Product> findById(@PathVariable("id") String id) {
        // Mono<Product> productMono = productDao.findById(id);
        /*return productDao.findAll().filter(p -> p.getId().equals(id))
                .next()
                .doOnNext(prod -> log.info(prod.getName()));*/
        return productService.findById(id)
                .doOnNext(prod -> log.info(prod.getName()));
    }

    @PostMapping
    public Mono<Product> create(@RequestBody Product product) {
        return productService.save(product);
    }

    @PutMapping("/{id}")
    public Mono<Product> update(@RequestBody Product product, @PathVariable("id") String id) {
        return productService.update(product, id);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return productService.delete(id);
    }
}
