package com.codearp.springboot.reactor.app.runners;

import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;

public class Reactive01CrearFlux implements CommandLineRunner {

    public static void main(String[] args) {
        CommandLineRunner runner = new Reactive01CrearFlux();
        try {
            runner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Reactive01CrearFlux run");

        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");

        // Subscribe with a simple consumer
        names.subscribe(System.out::println);

        // Subscribe with full consumer (onNext, onError, onComplete)
        names.subscribe(
            name -> System.out.println("Name: " + name),
            error -> System.err.println("Error: " + error),
            () -> System.out.println("Completed")
        );
    }
}
