package com.codearp.springboot.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Crear un observable basico de string
 * 1. Mostar los elemenots que emite
 * 2. Elimie los elementoes que empieza por 'A'
 * 2. Subcribirse y mostrar los elmentos que se han emitido
 */
public class ProjectReacto02CrearObservable {

    private static final Logger log = LoggerFactory.getLogger(ProjectReacto02CrearObservable.class);

    public static void main(String[] args) {
        // Observable (emisor de datos)
        Flux<String> names = Flux.just("Andres","Juan","Pedro")
                //.filter(element->!element.startsWith("A")) // El orden importa
                // cuando emite imprime
                .doOnNext(System.out::println)
                .filter(element->!element.startsWith("A"));

        // subscibe, hace algo cuando emite
        names.subscribe(log::info);
    }
}
