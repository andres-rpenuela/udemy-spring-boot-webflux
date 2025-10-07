package com.codearp.springboot.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Crear un observable basico de string
 * 1. Mostar los elemenots que emite
 * 2. Lanza un error si esta vacio
 * 3. Subcribirse y mostrar los elmentos que se han emitido o mensaje de error
 * 4. Completar la emsion
 */
public class ProjectReacto04CrearObservable {

    private static final Logger log = LoggerFactory.getLogger(ProjectReacto04CrearObservable.class);

    public static void main(String[] args) {
        // Observable (emisor de datos)
        Flux<String> names = Flux.just("Andres","Juan","Pedro","Maria")
                // Simula un error
                .doOnNext( e -> {
                    if( e.isEmpty() ){ // Interrumpe el flujo, no se emiete los siguientes
                        throw new RuntimeException("Nombres no pueden ser vacíos");
                    }else{
                        System.out.println(e);
                    }
                });
                //.doOnComplete(() -> System.out.println("fin"));

        // subscibe, hace algo cuando emite
        names.subscribe(
                // next (cunamer)
                name -> log.info(name),
                // error (Consumer)
                error -> log.error(error.getMessage()),
                // complete (Runnamble)
                () -> new Runnable(){
                    @Override
                    public void run() {
                        log.info("Completado");
                    }
                }
        );
    }
}
