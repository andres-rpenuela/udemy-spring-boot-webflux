package com.codearp.springboot.reactor.springbootsebfluxapirest.repositories;

import com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Category;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CategoryDao extends org.springframework.data.repository.reactive.ReactiveCrudRepository<com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Category, String> {
    Mono<Category> findByName(String name);
}
