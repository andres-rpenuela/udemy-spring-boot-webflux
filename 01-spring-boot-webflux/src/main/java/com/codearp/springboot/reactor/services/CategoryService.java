package com.codearp.springboot.reactor.services;

import com.codearp.springboot.reactor.models.documents.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {
    Flux<Category> findAllCategories();
    Mono<Category> findCategoryById(String id);
    Mono<Category> saveCategory(Category category);
}
