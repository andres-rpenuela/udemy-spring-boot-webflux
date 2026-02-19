package com.codearp.springboot.reactor.springbootsebfluxapirest.services.impls;

import com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Category;
import com.codearp.springboot.reactor.springbootsebfluxapirest.shared.CATEGORY;
import com.codearp.springboot.reactor.springbootsebfluxapirest.repositories.CategoryDao;
import com.codearp.springboot.reactor.springbootsebfluxapirest.services.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    @Override
    public Mono<CATEGORY> findById(String id) {
        return categoryDao.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Category not found with id: " + id)))
                .map(CATEGORY::fromCategory)
                .onErrorResume(e -> {
                    log.error("Error retrieving category by id {}: {}", id, e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<CATEGORY> findByName(String name) {
        return categoryDao.findByName(name)
                .switchIfEmpty(Mono.error(new RuntimeException("Category not found with name: " + name)))
                .map(CATEGORY::fromCategory)
                .onErrorResume(e -> {
                    log.error("Error retrieving category by name {}: {}", name, e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<Void> saveCategory(CATEGORY category) {

        return Mono.justOrEmpty(category)
                .map(catDto -> {
                    Category categoryDoc = new Category();
                    categoryDoc.setName(catDto.name());
                    return categoryDoc;
                })
                .flatMap(categoryDao::save)
                .then()
                .onErrorResume(e -> {
                    log.error("Error saving category {}: {}", category.name(), e.getMessage());
                    return Mono.error(e);
                });
    }
}
