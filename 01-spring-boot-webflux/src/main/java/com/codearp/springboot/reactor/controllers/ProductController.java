package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.dao.ProductDao;
import com.codearp.springboot.reactor.models.documents.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
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

    @GetMapping("/products-data-driver-bloqueante")
    public String listProductsDataDriverBloqueante(Model model) {

        // Obtener el Flux y transformar nombres a mayúsculas
        Flux<Product> productsFlux = productDao.findAll()
                .map(p -> {
                    p.setName(p.getName() != null ? p.getName().toUpperCase() : null);
                    return p;
                })
                // Simular retardo de 1 segundo por elemento
                // Para ver el efecto del data driver en la vista
                // esto hace que los elementos se emitan uno a uno con un retraso
                // y no todos de golpe, lo que permite ver la carga progresiva en la vista (en caso de usar clientes reactivos) o
                // o espera para visualizar hasta que todos esten cargados (en caso de usar thymeleaf normal)
                .delayElements(java.time.Duration.ofSeconds(1));

        // Suscribirse para imprimir los nombres de los productos - observer one
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        // observer two - thymeleaf view
        model.addAttribute("products", productsFlux);
        return "products/list";
    }


    @GetMapping("/products-data-driver")
    public String listProductsDataDriver(Model model) {

        // Obtener el Flux y transformar nombres a mayúsculas
        Flux<Product> productsFlux = productDao.findAll()
                .map(p -> {
                    p.setName(p.getName() != null ? p.getName().toUpperCase() : null);
                    return p;
                })
                // Simular retardo de 1 segundo por elemento
                // Para ver el efecto del data driver en la vista
                // esto hace que los elementos se emitan uno a uno con un retraso
                // y no todos de golpe, lo que permite ver la carga progresiva en la vista (en caso de usar clientes reactivos) o
                // o espera para visualizar hasta que todos esten cargados (en caso de usar thymeleaf normal)
                .delayElements(java.time.Duration.ofSeconds(1));

        // Suscribirse para imprimir los nombres de los productos - observer one
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        // observer two - thymeleaf view
        // se modifica el buffer de datos para que Thymeleaf procese los elementos a medida que llegan
        model.addAttribute("products", new ReactiveDataDriverContextVariable(productsFlux, 2));
        return "products/list";
    }
}
