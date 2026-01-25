package com.codearp.springboot.reactor.services;

import com.codearp.springboot.reactor.dao.ProductDao;
import com.codearp.springboot.reactor.models.documents.Category;
import com.codearp.springboot.reactor.models.documents.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final CategoryService categoryService;

    @Override
    public Flux<Product> findAll() {

        return productDao.findAll();
    }

    @Override
    public Flux<Product> findAllUpperCaseNames() {
        return productDao.findAll()
                .map(prod -> {
                    prod.setName(prod.getName() != null ? prod.getName().toUpperCase() : null);
                    return prod;
                });
    }

    @Override
    public Flux<Product> findAllUpperCaseNamesRepeat() {
        return findAllUpperCaseNames()
                .repeat(5000);
    }

    @Override
    public Mono<Product> findById(String id) {
        return productDao.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")));
                //.switchIfEmpty(Mono.error(new InterruptedException("Product not found")));
    }

    @Override
    public Mono<Product> save(Product product) {

        if( product.getCreateAt() == null ) {
            product.setCreateAt(new java.util.Date());
        }

        // Si no hay categoría en el producto, simplemente guardamos el producto.
        if (product.getCategory() == null) {
            return productDao.save(product);
        }

        // Si la categoría existe pero NO trae id -> no crearla: guardar producto sin categoría
        String catId = product.getCategory().getId();

        if (catId == null || catId.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID is required"));
        }

        // Si viene id: intentar obtenerla; si no existe, devolver 404
        Mono<Category> categoryMono = categoryService.findCategoryById(catId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")));

        // Componer: una vez que tengamos la categoría, asignarla y guardar el producto.
        return categoryMono.flatMap(cat -> {
            product.setCategory(cat);
            return productDao.save(product);
        });
    }

    @Override
    public Mono<Product> update(Product product, String id) {
        // Devolver el flujo resultante: si existe, actualizar y guardar; si no existe, devolver error 404

        return productDao.findById(id)
                .flatMap(p -> {
                    p.setName(product.getName());
                    p.setPrice(product.getPrice());
                    p.setPicture(product.getPicture());

                    if( product.getCategory().getId() != p.getCategory().getId() ) {
                        // Si la categoría ha cambiado, validar que la nueva exista
                        return categoryService.findCategoryById(product.getCategory().getId())
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")))
                                .flatMap(cat -> {
                                    p.setCategory(cat);
                                    return productDao.save(p);
                                });
                    }
                    return productDao.save(p);
                })
                //.switchIfEmpty(Mono.error(new InterruptedException("Product not found")));
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")));

    }

    @Override
    public Mono<Void> delete(String id) {
        // SWITCHIFEMPTY debe aplicarse sobre findById (Mono<Product>),
        // porque deleteById devuelve Mono<Void> (completa sin emitir) y
        // switchIfEmpty aplicado al Mono<Void> siempre se ejecutaría.
        return productDao.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))) // Mono.error, corta el flujo y evita flatMap.
                .flatMap(p -> productDao.deleteById(id));
    }
}
