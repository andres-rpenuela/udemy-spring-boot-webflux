package com.codearp.springboot.reactor.springbootsebfluxapirest.services.impls;

import com.codearp.springboot.reactor.springbootsebfluxapirest.shared.CATEGORY;
import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import com.codearp.springboot.reactor.springbootsebfluxapirest.repositories.CategoryDao;
import com.codearp.springboot.reactor.springbootsebfluxapirest.repositories.ProductDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements com.codearp.springboot.reactor.springbootsebfluxapirest.services.ProductService {

    private final ProductDao productDao;
    private final CategoryDao categoryDao;

    @Override
    public Flux<ProductDto> recoverALlProduct() {
        return productDao.findAll()
                .map(product -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setId(product.getId());
                    productDto.setName(product.getName());
                    productDto.setPrice(product.getPrice());
                    productDto.setCreateAt(product.getCreateAt());
                    if (product.getCategory() != null) {
                        productDto.setCategory(CATEGORY.fromCategory(product.getCategory()));
                    }
                    return productDto;
                })
                .onErrorResume(e -> {
                    log.error("Error retrieving all products: {}", e.getMessage());
                    return Flux.error(e);
                });

    }

    @Override
    public Mono<ProductDto> recoverProductById(String id) {
        return productDao.findById(id)
                .map(product -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setId(product.getId());
                    productDto.setName(product.getName());
                    productDto.setPrice(product.getPrice());
                    productDto.setCreateAt(product.getCreateAt());
                    if (product.getCategory() != null) {
                        productDto.setCategory(CATEGORY.fromCategory(product.getCategory()));
                    }
                    return productDto;
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + id)))
                .onErrorResume(e -> {
                    log.error("Error retrieving product by id {}: {}", id, e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<ProductDto> saveProduct(ProductDto productDto) {
        if (productDto.getCreateAt() == null) {
            productDto.setCreateAt(new Date() );
        }

        if(productDto.getCategory() == null) {
            productDto.setCategory(CATEGORY.OTHER);
        }

        return categoryDao.findByName(productDto.getCategory().name())
                .switchIfEmpty(Mono.error(new RuntimeException("Category not found with name: " + productDto.getCategory().name())))
                .flatMap(cat -> {
                    com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Product product = new com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Product();
                    product.setName(productDto.getName());
                    product.setPrice(productDto.getPrice());
                    product.setCreateAt(productDto.getCreateAt());
                    product.setCategory(cat);
                    return productDao.save(product);
                })
                .map(p -> {
                    productDto.setId( p.getId() );
                    return productDto;
                })
                .onErrorResume(e -> {
                    log.error("Error saving product {}: {}", productDto.getName(), e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<Void> deleteProductById(String id) {

        return productDao.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + id)))
                .flatMap(productDao::delete)
                .onErrorResume(e -> {
                    log.error("Error deleting product with id {}: {}", id, e.getMessage());
                    return Mono.error(e);
                });
    }
}
