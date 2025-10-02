package com.codearp.springboot.reactor;

import reactor.core.publisher.Flux;

/**
 * Crear un observable basico de string
 * 1. Mostar los elemenots que emite
 * 2. Subcribirse y mostrar los elmentos que se han emitido
 */
public class ProjectReacto01CrearObservable {

    public static void main(String[] args) {
        // Observable (emisor de datos)
        Flux<String> names = Flux.just("Andres","Juan","Pedro")
                // cuando emite imprime
                .doOnNext(System.out::println); // opcional

        // subscibe, hace algo cuando emite
        names.subscribe(); //
        //names.subscribe(System.out::println);
    }
}
