package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.dao.ProductDao;
import com.codearp.springboot.reactor.models.documents.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductDao productDao;


    @GetMapping("/products")
    public String listProducts(Model model) {

        // Obtener el Flux y transformar nombres a mayúsculas
        Flux<Product> productsFlux = productDao.findAll();
        productsFlux = productsFlux
                .map(p -> {
                    p.setName(p.getName() != null ? p.getName().toUpperCase() : null);
                    return p;
                });

        // Suscribirse para imprimir los nombres de los productos - observer one
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        // observer two - thymeleaf view
        model.addAttribute("products", productsFlux);
        return "products/list";
    }
}
