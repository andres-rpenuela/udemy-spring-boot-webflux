package com.codearp.springboot.reactor.dao;

import com.codearp.springboot.reactor.models.documents.Category;
import jdk.jfr.Registered;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@Registered
public interface CategoryDao extends ReactiveCrudRepository<Category, String> {
}
