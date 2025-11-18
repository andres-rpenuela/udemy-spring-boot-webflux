package com.codearp.springboot.reactor.app.runners;

import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.Scanner;

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
        int opt = 0;
        Scanner sc = new Scanner(System.in);
        do{
            System.out.print("""
                Operaciones:
                \t0. Create Mono (con just).
                \t1. Create Flux (con just).
                \t2. Create Flux from Iterable.
                \t3. Create Flux from Array.
                \t4. Create Flux from Stream.
                \t5. Create Flux from Range.
                \t6. Create Flux from Interval.
                \t7. Create Mono / Flux from Create
                \t8. Create Flux from Generate.
                \t9. Create Mono / Flux from defer.
                \t10. Create Mono / Flux from Empty.
                \t11. Create Mono / Flux from Error.
                \t12. Create Mono from Callable.
                \t13. Create Mono from Future.
                \t14. Create Mono / Flux from Publisher.
                \t15. Convert Mono to Flux.
                \t16. Convert Flux to Mono.
                \t17. Run task (Mono) without return value.
                \t18. Create Mono from Supplier.
                \t19. Create Mono from Completabe Future.
                \t20. Salir.
                Elige una opcion: \s""");

            try {
                opt = Integer.parseInt( sc.nextLine() );
                switch (opt) {
                    case 0 -> createMono();
                    case 1 -> createFlux();
                    case 2 -> createFromIterable();
                    case 3 -> createFromArray();
                    case 4 -> createFromStream();
                    case 5 -> createFromRange();
                    case 6 -> createInterval();
                    case 7 -> createMonoAndFluxFromCreate();
                    case 8 -> createFluxFromGenerate();
                    case 9 -> createMonoAndFluxFromDefer();
                    case 10 -> createMonoAndFluxEmpty();
                    case 11 -> createMonoAndFluxError();
                    case 12 -> createMonoFromCallable();
                    case 13 -> createMonoFromFuture();
                    case 14 -> createMonoAndFluxFromPublisher();
                    case 15 -> convertMonoToFlux();
                    case 16 -> convertFluxToMono();
                    case 17 -> runTaskWithoutReturnValue(); // fromRunnable()
                    case 18 -> createMonoFromSupplier();
                    case 19 -> createMonoFromCompletableFuture();
                    case 24 -> opt = -1;
                    default -> System.out.println("Default");
                }
            }catch (Exception e){
                System.out.println("Error: " + e.getMessage());
                opt = 0;
            }


        }while (opt != -1);

    }

    public static void createMonoFromFuture() {
        System.out.println("========================================");
        System.out.println("Create Mono from Future");
        java.util.concurrent.CompletableFuture<String> future = java.util.concurrent.CompletableFuture.supplyAsync(() -> "Mono from Future");

        Mono<String> monoFromFuture = Mono.fromFuture(future);

        monoFromFuture.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Future")
        );
    }

    public static void createMonoFromSupplier() {
        System.out.println("========================================");
        System.out.println("Create Mono from Supplier");
        java.util.function.Supplier<String> supplier = () -> "Mono from Supplier";

        Mono<String> monoFromSupplier = Mono.fromSupplier(supplier);

        monoFromSupplier.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Supplier")
        );
    }

    public static void runTaskWithoutReturnValue() {
        System.out.println("========================================");
        System.out.println("Run task (Mono) without return value");
        Runnable task = () -> {
            // Simulate some task
            try {
                Thread.sleep(1000);
                System.out.println("Task executed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Mono<Void> monoFromRunnable = Mono.fromRunnable(task);

        monoFromRunnable.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Runnable")
        );
    }

    public static void convertFluxToMono() {
        System.out.println("========================================");
        System.out.println("Convert Flux to Mono");
        Flux<String> flux = Flux.just("Flux to Mono Example - 1", "Flux to Mono Example - 2", "Flux to Mono Example - 3");

        Mono<String> monoFromFlux = flux.reduce( (a, b) -> a + ", " + b );

        monoFromFlux.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Flux")
        );
    }

    public static void convertMonoToFlux() {
        System.out.println("========================================");
        System.out.println("Convert Mono to Flux");
        Mono<String> mono = Mono.just("Mono to Flux Example");

        Flux<String> fluxFromMono = mono.flux();

        fluxFromMono.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Mono")
        );
    }

    private static void createMonoAndFluxFromPublisher() {
        System.out.println("========================================");
        System.out.println("Create Mono and Flux from Publisher");

        java.util.concurrent.Flow.Publisher<String> publisherMono = new java.util.concurrent.Flow.Publisher<>() {
            @Override
            public void subscribe(java.util.concurrent.Flow.Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new java.util.concurrent.Flow.Subscription() {
                    private boolean completed = false;

                    @Override
                    public void request(long n) {
                        if (!completed) {
                            subscriber.onNext("Mono from Publisher");
                            subscriber.onComplete();
                            completed = true;
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        };

        Mono<String> monoFromPublisher = Mono.from((Publisher<? extends String>) publisherMono);

        monoFromPublisher.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Publisher")
        );

        System.out.println("----------------------------------------");

        java.util.concurrent.Flow.Publisher<String> publisherFlux = new java.util.concurrent.Flow.Publisher<>() {
            @Override
            public void subscribe(java.util.concurrent.Flow.Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new java.util.concurrent.Flow.Subscription() {
                    private int count = 0;
                    private final int max = 5;

                    @Override
                    public void request(long n) {
                        for (int i = 0; i < n && count < max; i++) {
                            subscriber.onNext("Flux from Publisher - " + (++count));
                        }
                        if (count >= max) {
                            subscriber.onComplete();
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        };

        Flux<String> fluxFromPublisher = Flux.from((Publisher<? extends String>) publisherFlux);

        fluxFromPublisher.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Publisher")
        );
    }

    private static void createMonoFromCompletableFuture() {
        System.out.println("========================================");
        System.out.println("Create Mono from Completable Future");
        java.util.concurrent.CompletableFuture<String> completableFuture = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            // Simulate some computation or blocking call
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Mono from Completable Future";
        });

        Mono<String> monoFromCompletableFuture = Mono.fromFuture(completableFuture);

        monoFromCompletableFuture.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Completable Future")
        );
    }

    private static void createMonoFromCallable() {
        System.out.println("========================================");
        System.out.println("Create Mono from Callable");
        Mono<String> monoFromCallable = Mono.fromCallable(() -> {
            // Simulate some computation or blocking call
            Thread.sleep(1000);
            return "Mono from Callable";
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()); // Use boundedElastic scheduler for blocking calls and not to block main thread

        monoFromCallable.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Callable")
        );
    }

    private static void createMonoAndFluxError() {
        System.out.println("========================================");
        System.out.println("Create Mono and Flux from Error");
        Mono<String> monoError = Mono.error(new RuntimeException("Mono Error Example"));

        monoError.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Error")
        );
        System.out.println("----------------------------------------");
        Flux<String> fluxError = Flux.error(new RuntimeException("Flux Error Example"));

        fluxError.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Error")
        );
    }

    private static void createMonoAndFluxEmpty() {
        System.out.println("========================================");
        System.out.println("Create Mono and Flux from Empty");
        Mono<String> monoEmpty = Mono.empty();

        monoEmpty.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Empty")
        );
        System.out.println("----------------------------------------");
        Flux<String> fluxEmpty = Flux.empty();

        fluxEmpty.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Empty")
        );
    }

    private static void createMonoAndFluxFromDefer() {
        System.out.println("========================================");
        System.out.println("Create Mono and Flux from Defer");
        Mono<String> monoFromDefer = Mono.defer( () -> Mono.just("Mono from Defer") );

        monoFromDefer.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Defer")
        );
        System.out.println("----------------------------------------");
        Flux<String> fluxFromDefer = Flux.defer( () -> Flux.just("Flux from Defer - 1", "Flux from Defer - 2", "Flux from Defer - 3") );

        fluxFromDefer.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Defer")
        );

    }

    private static void createFluxFromGenerate() {
        System.out.println("========================================");
        System.out.println("Create Flux from Generate");

        Flux<String> fluxFromGenerate = Flux.generate(
                () -> 1, // Estado inicial
                (state, sink) -> {
                    sink.next("Flux from Generate - " + state);
                    if (state >= 5) {
                        sink.complete();
                    }
                    return state + 1; // Nuevo estado
                }
        );

        fluxFromGenerate.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Generate"),
                subscription -> subscription.request(1) // Solicitar más elementos si es necesario
        );
    }
    private static void createMonoAndFluxFromCreate() {
        System.out.println("========================================");
        System.out.println("Create Mono and Flux from Create");

        Mono<String> monoFromCreate = Mono.create( sink ->{
            sink.success("Mono from Create");
        });

        monoFromCreate.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono from Create")
        );
        System.out.println("----------------------------------------");

        Flux<String> fluxFromCreate = Flux.create( sink ->{
            sink.next("Flux from Create - 1");
            sink.next("Flux from Create - 2");
            sink.next("Flux from Create - 3");
            sink.complete();
        });
        fluxFromCreate.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Create")
        );
    }

    private static void createInterval() {
        System.out.println("========================================");
        System.out.println("Create Flux from Interval");
        Flux<Long> fluxFromInterval = Flux.interval(java.time.Duration.ofSeconds(1)).take(5);

        fluxFromInterval.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Interval")
        );

        // Sleep the main thread to allow the interval to emit values
        try {
            Thread.sleep(6000); // Sleep for 6 seconds to see all emissions
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createMono(){
        System.out.println("========================================");
        System.out.println("Create Mono");
        Mono<String> monoName = Mono.just("Andres");

        monoName.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Mono")
        );
        System.out.println("========================================");
    }


    private static void createFlux(){
        System.out.println("========================================");
        System.out.println("Create Flux");
        Flux<String> fluxNames = Flux.just("Andres", "John", "Jane", "Doe");

        fluxNames.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux")
        );
        System.out.println("========================================");
    }

    private static void createFromIterable() {
        System.out.println("========================================");
        System.out.println("Create Flux from Iterable");
        Iterable<String> namesIterable = java.util.List.of("Andres", "John", "Jane", "Doe");
        Flux<String> fluxFromIterable = Flux.fromIterable(namesIterable);

        fluxFromIterable.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Iterable")
        );
        System.out.println("========================================");
    }

    private static void createFromArray() {
        System.out.println("========================================");
        System.out.println("Create Flux from Array");
        String[] namesArray = {"Andres", "John", "Jane", "Doe"};
        Flux<String> fluxFromArray = Flux.fromArray(namesArray);

        fluxFromArray.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Array")
        );
        System.out.println("========================================");
    }

    private static void createFromStream() {
        System.out.println("========================================");
        System.out.println("Create Flux from Stream");
        java.util.stream.Stream<String> namesStream = java.util.stream.Stream.of("Andres", "John", "Jane", "Doe");
        Flux<String> fluxFromStream = Flux.fromStream(namesStream);

        fluxFromStream.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Stream")
        );
        System.out.println("========================================");
    }

    private static void createFromRange() {
        System.out.println("========================================");
        System.out.println("Create Flux from Range");
        Flux<Integer> fluxFromRange = Flux.range(1, 5);

        fluxFromRange.subscribe(
                data -> System.out.println("Data: " + data),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed Flux from Range"),
                subscription -> subscription.request(10) // Solicitar más elementos si es necesario
        );
        System.out.println("========================================");
    }

}
