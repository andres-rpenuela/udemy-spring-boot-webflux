package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.models.documents.Product;
import com.codearp.springboot.reactor.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes("product") // Mantiene un producto en sesión mientras se edita/crea, no es recomendable en WebFlux
public class ProductController {

    private final ProductService productService;

//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        sdf.setLenient(false);
//        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
//    }

    // ------------------------------------------------------------
    // LISTADO DE PRODUCTOS
    // ------------------------------------------------------------

    // Listado normal
    @GetMapping("/products")
    public String listProducts(Model model) {
        Flux<Product> productsFlux = productService.findAllUpperCaseNames();
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        model.addAttribute("products", productsFlux);
        return "products/list";
    }

    // Listado con retraso simulado para ver efecto DataDriver
    @GetMapping("/products-data-driver")
    public String listProductsDataDriver(Model model) {
        Flux<Product> productsFlux = productService.findAllUpperCaseNames()
                .delayElements(java.time.Duration.ofSeconds(1));

        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        // ReactiveDataDriverContextVariable permite procesar los elementos a medida que llegan
        model.addAttribute("products", new ReactiveDataDriverContextVariable(productsFlux, 2));
        return "products/list";
    }

    // Listado chunked (para grandes cantidades de datos)
    @GetMapping("/products-chunked")
    public String listProductsChunked(Model model) {
        Flux<Product> productsFlux = productService.findAllUpperCaseNamesRepeat();
        productsFlux.subscribe(p -> log.info(p.getName()));

        model.addAttribute("title", "Product List");
        model.addAttribute("products", productsFlux);
        return "products/list";
    }

    // ------------------------------------------------------------
    // FORMULARIO DE CREACIÓN
    // ------------------------------------------------------------

    // GET formulario nuevo producto (recomendado en WebFlux)
    @GetMapping("/products/form")
    public Mono<String> newProductForm(Model model) {

        model.addAttribute("product", new Product());
        model.addAttribute("title", "New Product");
        return Mono.just("products/form");
    }

    // POST crear producto (reactivo)
    @PostMapping("/products/form")
    public Mono<String> saveProduct(
            @Valid @ModelAttribute("product") Product product,
            Errors errors,
            Model model) {

        if (errors.hasErrors()) {
            errors.getFieldErrors()
                    .forEach(err -> log.error("Field error: {} - {}", err.getField(), err.getDefaultMessage()));

            model.addAttribute("title", "New Product");
            return Mono.just("products/form"); // Mantener la vista sin redirect
        }

        return productService.save(product)
                .doOnNext(p -> log.info("Created product: {}", p.getName()))
                .thenReturn("redirect:/products"); // Redirigir solo si no hay errores
    }

    // ------------------------------------------------------------
    // FORMULARIO DE EDICIÓN
    // ------------------------------------------------------------

    @GetMapping({"/products/edit/{id}", "/products/form/{id}"})
    public Mono<String> editProductForm(@PathVariable("id") String id, Model model){
        return productService.findById(id)
                .doOnNext(p -> log.info("Editing product: {}", p.getName()))
                .flatMap(p -> {
                    model.addAttribute("title", "Edit Product");
                    model.addAttribute("product", p);
                    return Mono.just("products/form");
                })
                .onErrorResume(ex -> Mono.just("redirect:/products?error=Product+Not+Found"));
    }

    // POST/PUT actualizar producto (anti-patrón: basado en redirect + BindingResult)
    @RequestMapping(value = "/products/form/{id}", method = { RequestMethod.POST, RequestMethod.PUT } )
    public Mono<String> updateProduct(@Valid @ModelAttribute Product product,
                                      Errors errors,
                                      @PathVariable("id") String id,
                                      Model model,
                                      SessionStatus status) {

        if (errors.hasErrors()) {
            errors.getFieldErrors()
                    .forEach(err -> log.error("Field error: {} - {}", err.getField(), err.getDefaultMessage()));

            model.addAttribute("title", "Edit Product");
            return Mono.just("products/form"); // Mantener la vista sin redirect
        }

        return productService.update(product, id)
                .doOnNext(p -> log.info("Updated product: {}", p.getName()))
                .doOnSuccess(p -> status.setComplete())
                .thenReturn("redirect:/products?success=Product+Updated")
                .onErrorResume(ex -> Mono.just("redirect:/products?error=Product+Not+Found"));
    }

    // ------------------------------------------------------------
    // ELIMINAR PRODUCTO
    // ------------------------------------------------------------
    @RequestMapping(value="/products/eliminar/{id}", method = { RequestMethod.GET, RequestMethod.DELETE } )
    public Mono<String> deleteProduct(@PathVariable("id") String id) {
        // opcion 1
//        return productService.findById(id)
//                .map(Product::getId)
//                .flatMap(productService::delete)
//                .doOnSuccess(v -> log.info("Deleted product with id: {}", id))
//                .thenReturn("redirect:/products?success=Product+Deleted")
//                .onErrorResume(ex -> Mono.just("redirect:/products?error=Product+Not+Found"));

        // opcion 2
        return productService.delete(id)
                .doOnSuccess(v -> log.info("Deleted product with id: {}", id))
                .thenReturn("redirect:/products?success=Product+Deleted")
                .onErrorResume(ex -> Mono.just("redirect:/products?error=Product+Not+Found"));

    }

    // ------------------------------------------------------------
    // NOTAS IMPORTANTES WEBFLUX
    // ------------------------------------------------------------

    /*
     * 1️⃣ Evitar usar BindingResult, WebSession, SessionStatus → patrón MVC.
     * 2️⃣ En WebFlux, los errores se mantienen en la misma request.
     * 3️⃣ Redirect solo cuando la operación fue exitosa.
     * 4️⃣ newProductForm y editProductForm pueden reutilizar la misma vista "products/form".
     * 5️⃣ Para listas grandes: usar ReactiveDataDriverContextVariable o chunked rendering.
     */

    /**
     * ✅ Claves para apuntes
     *
     * WebFlux no necesita BindingResult → usa Errors.
     * No redirigir si hay errores, mantén la misma vista.
     * SessionAttributes solo para mantener un producto en edición; no usar WebSession para errores.
     * ReactiveDataDriverContextVariable → render progresivo de listas.
     * Un solo template form sirve para crear y editar.
     * Logs y doOnNext → útiles para debug reactivo.
     */
}
