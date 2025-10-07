package com.codearp.springboot.reactor.app.runners;

import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Reactive02OperationFlux implements CommandLineRunner {

    public static void main(String[] args) {
        CommandLineRunner runner = new Reactive02OperationFlux();
        try {
            runner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Reactive01CrearFlux run");

        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
                .map(String::toUpperCase)
                .filter(name -> name.length() >= 4)
                .doOnNext(name -> {
                    if (name.equals("JANE")) {
                        throw new RuntimeException("Error processing name: " + name);
                    }
                })
                // Log each name processed
                .log()
                // Handle error by providing a default value
                .onErrorResume(e -> {
                    System.err.println("Caught error: " + e.getMessage());
                    return Mono.just("DEFAULT");
                });

        // Subscribe with full consumer (onNext, onError, onComplete)
        names.subscribe(
            name -> System.out.println("Name: " + name),
            error -> System.err.println("Error: " + error),
            () -> System.out.println("Completed") // Si no se captura el error `onErrorResume`, no se ejecuta este bloque
        );
    }
}
