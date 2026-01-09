package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.models.documents.Product;
import com.codearp.springboot.reactor.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes("product") // Cada vez que se cargue un producto en el modelo, se guardará en sesión
public class ProductController {

    private final ProductService productService;

//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        sdf.setLenient(false);
//        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
//    }


    @GetMapping("/products")
    public String listProducts(Model model) {

        // Obtener el Flux y transformar nombres a mayúsculas
        Flux<Product> productsFlux = productService.findAllUpperCaseNames();

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
        Flux<Product> productsFlux = productService.findAllUpperCaseNames()
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
        Flux<Product> productsFlux = productService.findAllUpperCaseNames()
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
        // número de elementos que se procesan en cada "chunk"
        model.addAttribute("products", new ReactiveDataDriverContextVariable(productsFlux, 2));
        return "products/list";
    }

    @GetMapping("/products-chunked")
    public String listProductsChunked(Model model) {

        // Obtener el Flux y transformar nombres a mayúsculas
        // Simular un flujo de elementos grande
        Flux<Product> productsFlux = productService.findAllUpperCaseNamesRepeat();

        // Suscribirse para imprimir los nombres de los productos - observer one
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        // observer two - thymeleaf view
        // se modifica el buffer de datos para que Thymeleaf procese los elementos a medida que llegan
        // creando "chunks" o bloques de datos, recomendado para grandes cantidades de informacion
        model.addAttribute("products", productsFlux);
        return "products/list";
    }

    @GetMapping("/products-chunked-view-names")
    public String listProductsChunkedViewNames(Model model) {

        // Obtener el Flux y transformar nombres a mayúsculas
        // Simular un flujo de elementos grande
        Flux<Product> productsFlux = productService.findAllUpperCaseNamesRepeat();

        // Suscribirse para imprimir los nombres de los productos - observer one
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        // observer two - thymeleaf view
        // se modifica el buffer de datos para que Thymeleaf procese los elementos a medida que llegan
        // creando "chunks" o bloques de datos, recomendado para grandes cantidades de informacion
        model.addAttribute("products", productsFlux);
        return "products/list-chunked";
    }

    @GetMapping("/products/form")
    public String newProductForm(Model model) {
        model.addAttribute("title", "New Product");
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @PostMapping("/products/form")
    public Mono<String> saveProduct(@ModelAttribute Product product, SessionStatus status) {
        // Guardar el producto y redirigir a la lista
        return productService.save(product)
                .doOnNext(p -> log.info("Created product: " + p.getName()))
                .doOnSuccess(p -> status.setComplete())
                //.then( Mono.just("redirect:/products"))
                .thenReturn("redirect:/products");
    }

    @GetMapping({"/products/edit/{id}", "/products/form/{id}"})
    public Mono<String> editProductForm(@PathVariable("id") String id, Model model){
        return productService.findById(id)
                .doOnNext(p -> log.info("Editing product: " + p.getName()))
                .flatMap( p -> {
                    model.addAttribute("title", "Edit Product");
                    model.addAttribute("product", p);
                    return Mono.just("products/form");
                });
    }


    @RequestMapping(value = "/products/form/{id}", method = { RequestMethod.POST, RequestMethod.PUT } )
    public Mono<String> updateProduct(@ModelAttribute Product product, @PathVariable("id") String id, SessionStatus status) {
        // Actualizar el producto y redirigir a la lista
        return productService.update(product, id)
                .doOnNext(p -> log.info("Updated product: " + p.getName()))
                .doOnSuccess( p -> status.setComplete())
                //.then( Mono.just("redirect:/products"))
                .thenReturn("redirect:/products");
    }
}
