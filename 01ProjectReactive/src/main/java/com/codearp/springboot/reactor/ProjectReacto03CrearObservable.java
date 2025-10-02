package com.codearp.springboot.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Crear un observable basico de string
 * 1. Mostar los elemenots que emite
 * 2. Subcribirse y mostrar los elmentos que se han emitido
 */
public class ProjectReacto03CrearObservable {

    private static final Logger log = LoggerFactory.getLogger(ProjectReacto03CrearObservable.class);

    public static void main(String[] args) {
        // Observable (emisor de datos)
        Flux<String> names = Flux.just("Andres","Juan","Pedro","","Maria")
                // Simula un error
                .doOnNext( e -> {
                    if( e.isEmpty() ){ // Interrumpe el flujo, no se emiete los siguientes
                        throw new RuntimeException("Nombres no pueden ser vacíos");
                    }else{
                        System.out.println(e);
                    }
                });

        // subscibe, hace algo cuando emite
        names.subscribe(
                name -> log.info(name),
                error -> log.error(error.getMessage())
        );
    }
}
