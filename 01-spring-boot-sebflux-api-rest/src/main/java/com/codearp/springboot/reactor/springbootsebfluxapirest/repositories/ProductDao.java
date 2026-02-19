package com.codearp.springboot.reactor.springbootsebfluxapirest.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDao extends ReactiveCrudRepository<com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Product, String> {

}
