package com.codearp.springboot.reactor.springbootsebfluxapirest.services;

import com.codearp.springboot.reactor.springbootsebfluxapirest.shared.CATEGORY;
import reactor.core.publisher.Mono;

public interface CategoryService {

    Mono<CATEGORY> findById(String id);
    Mono<CATEGORY> findByName(String name);

    Mono<Void> saveCategory(CATEGORY category);

}
