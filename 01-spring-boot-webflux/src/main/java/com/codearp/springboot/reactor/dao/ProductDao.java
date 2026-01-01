package com.codearp.springboot.reactor.dao;

import com.codearp.springboot.reactor.models.documents.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDao extends ReactiveCrudRepository<Product,String> {
}
