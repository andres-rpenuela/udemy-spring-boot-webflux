package com.codearp.springboot.reactor.springbootsebfluxapirest.facades;

import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopFacadeImpl implements ShopFacade {
    private final ProductService productService;

    @Override
    public Flux<ProductDto> recoverAllProducts() {
        return productService.recoverALlProduct()
                .onErrorResume(e -> {
                    log.error("Error in ShopFacade while retrieving products: {}", e.getMessage());
                    return Flux.error(e);
                });
    }

    @Override
    public Mono<ProductDto> recoverProductById(String id) {
        return productService.recoverProductById(id)
                .onErrorResume(e -> {
                    log.error("Error in ShopFacade while retrieving product with id {}: {}", id, e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<ProductDto> saveProduct(ProductDto productDto) {
        return productService.saveProduct(productDto)
                .onErrorResume(e -> {
                    log.error("Error in ShopFacade while saving product {}: {}", productDto.getName(), e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<Void> deleteProductById(String id) {
        return productService.deleteProductById(id)
                .onErrorResume(e -> {
                    log.error("Error in ShopFacade while deleting product with id {}: {}", id, e.getMessage());
                    return Mono.error(e);
                });
    }

}
