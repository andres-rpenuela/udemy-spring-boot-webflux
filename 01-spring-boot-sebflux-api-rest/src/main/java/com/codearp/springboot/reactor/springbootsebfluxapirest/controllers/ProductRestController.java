package com.codearp.springboot.reactor.springbootsebfluxapirest.controllers;

import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import com.codearp.springboot.reactor.springbootsebfluxapirest.facades.ShopFacade;
import com.codearp.springboot.reactor.springbootsebfluxapirest.facades.files.FileStorageFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ShopFacade shopFacade;
    private final FileStorageFacade fileStorageFacade;

    // La forma más simple y reactiva (streaming, sin ResponseEntity):
    @GetMapping({"","/"})
    public Flux<ProductDto> recoverAllProducts() {
        return shopFacade.recoverAllProducts()
                .onErrorMap(e -> new RuntimeException("Failed to retrieve products", e));
    }

    /**
     * This method demonstrates how to return a ResponseEntity with a Flux body. It allows you to set headers and status codes while still returning a reactive stream of products.
     * Note that the onErrorMap is used to handle any errors that may occur during the retrieval of products, wrapping them in a RuntimeException with a custom message.
     *
     * Se utiliza Mono.just para crear un Mono que emite un ResponseEntity. El cuerpo del ResponseEntity es un Flux de ProductDto obtenido del shopFacade. Esto permite que el controlador maneje la respuesta de manera reactiva, devolviendo un flujo de productos en lugar de una lista completa.
     * El método también incluye un manejo de errores utilizando onErrorMap para convertir cualquier error que ocurra durante la recuperación de productos en una RuntimeException con un mensaje personalizado.
     *
     * ResponeEntity no es un flujo reactivo en sí mismo, pero al envolver el Flux dentro de un ResponseEntity, puedes controlar los encabezados y el estado de la respuesta HTTP mientras sigues devolviendo un flujo de datos reactivo.
     *
     * @return
     */
    // Esta versión devuelve un ResponseEntity que contiene un Flux de ProductDto, lo que permite controlar los encabezados y el tipo de contenido de la respuesta HTTP.
    //
    @GetMapping({"/v2"})
    public Mono<ResponseEntity<Flux<ProductDto>>> recoverAllProductsV2() {
        return Mono.just(
                ResponseEntity.ok()
                .contentType( org.springframework.http.MediaType.APPLICATION_JSON)
                .body(shopFacade.recoverAllProducts() )
                )
                // este errorMap es para el Mono que envuelve el ResponseEntity, no para el Flux interno. Si quieres manejar errores del Flux, hazlo dentro del body() o en el shopFacade.
                .onErrorMap(e -> new RuntimeException("Failed to retrieve products", e));
    }

    // ontrolar el status (ej. devolver 204 cuando no hay elementos) y seguir enviando un Flux (mantener streaming), usa hasElements() sobre un Flux compartido/cached.
    @GetMapping(value = "/v3/{id}")
    public Mono<ResponseEntity<Flux<ProductDto>>> recoverAllProductsV3() {
        Flux<ProductDto> flux = shopFacade.recoverAllProducts()
                .doOnError(e -> {/* log si quieres */})
                .cache(); // permite múltiples suscripciones sin re-consultar la BD

        return flux.hasElements()
                .flatMap(has -> has
                        ? Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(flux))
                        : Mono.just(ResponseEntity.noContent().build())
                );
    }

    /*****************
     *  Find product by ID
     *
     *****************/
    @GetMapping("/{id}")
    public Mono<ProductDto> recoverProductById(@PathVariable String id) {
        // Implement logic to retrieve a single product by ID using the shopFacade
        return shopFacade.recoverProductById(id)
                .onErrorMap(e -> new RuntimeException("Failed to retrieve product with id: " + id, e));
    }

    @GetMapping("/v2/{id}")
    public Mono<ResponseEntity<ProductDto>> recoverProductByIdV2(@PathVariable String id) {
        return shopFacade.recoverProductById(id)
                .map(productDto -> ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(productDto)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build()) // Si el Mono está vacío, devuelve un 404
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping({"","/"})
    public Mono<ResponseEntity<ResponseEntity<ProductDto>>> saveProduct(@RequestBody ProductDto productDto) {
        return shopFacade.saveProduct(productDto)
                .map(savedProduct -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(savedProduct))
                .map(ResponseEntity::ok) // envuelve el ResponseEntity<ProductDto> en otro ResponseEntity
                .onErrorMap(e -> new RuntimeException("Failed to save product: " + productDto.getName(), e));
    }

    @PostMapping(
            value = "/v2",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_OCTET_STREAM_VALUE},
            produces = {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_OCTET_STREAM_VALUE}
    )
    public Mono<ResponseEntity<ProductDto>> saveProductWithImage(
            @RequestPart("product") ProductDto productDto,
            @RequestPart("image") FilePart image) {


        return  fileStorageFacade.saveFile(image)
                .flatMap(fileId -> {
                    productDto.setPicture(fileId.uuid().toString());
                    return shopFacade.saveProduct(productDto);
                })
                .map(savedProduct -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(savedProduct))
                .onErrorMap(e -> new RuntimeException("Failed to save product with image: " + productDto.getName(), e));
    }



    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return shopFacade.deleteProductById(id)
                .thenReturn(ResponseEntity.noContent().build()); // Devuelve 204 No Content si la eliminación es exitosa
    }
}
