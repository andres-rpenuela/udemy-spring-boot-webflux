package com.codearp.springboot.reactor.app.runners;

import com.codearp.springboot.reactor.app.dtos.UserDto;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Reactive03OperationFlux implements CommandLineRunner {

    public static void main(String[] args) {
        CommandLineRunner runner = new Reactive03OperationFlux();
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
                \t0. Inmutabilidad.
                \t1. createFluxFromIterable.
                \t2. createFluxFromRange.
                \t3. createFluxFromInterval.
                \t4. createFluxFromIntervalRange.
                \t5. createFluxFormArray.
                \t6. createFluxFromStream.
                \t7. createFluxFromMap.
                \t8. filterExample.
                \t9. mapExample.
                \t10. flatMapExample.
                \t11. concatMapExample.
                \t12. switchMapExample.
                \t13. groupByExample.
                \t14. reduceExample.
                \t15. scanExample.
                \t16. zipWithExample.
                \t17. combineLatestExample.
                \t18. zipWithTakeExample.
                \t19. zipWithRangeExample.
                \t20. delayElementsExample.
                \t21. backPressureExample.
                \t22. createExample.
                \t23. convertFluxToMonoExample.
                \t20. Salir.
                Elige una opcion: \s""");

            try {
                opt = Integer.parseInt( sc.nextLine() );
                switch (opt) {
                    case 0 -> immutabilityExample();
                    case 1 -> createFluxFromIterable();
                    case 2 -> createFluxFromRange();
                    case 3 -> createFluxFromInterval();
                    case 4 -> createFluxFromIntervalRange();
                    case 5 -> createFluxFormArray();
                    case 6 -> createFluxFromStream();
                    case 7 -> createFluxFromMap();
                    case 8 -> filterExample();
                    case 9 -> mapExample();
                    case 10 -> flatMapExample();
                    case 11 -> concatMapExample();
                    case 12 -> switchMapExmample();
                    case 13 -> groupByExample();
                    case 14 -> reduceExample();
                    case 15 -> scanExample();
                    case 16 -> zipWithExample();
                    case 17 -> combineLatestExample();
                    case 18 -> zipWithTakeExample();
                    case 19 -> zipWithRangeExample();
                    case 20 -> delayElementsExample();
                    case 21 -> backPressureExample();
                    case 22 -> createExample();
                    case 23 -> convertFluxToMonoExample();
                    case 24 -> opt = -1;
                    default -> System.out.println("Default");
                }
            }catch (Exception e){
                System.out.println("Error: " + e.getMessage());
                opt = 0;
            }


        }while (opt != -1);

    }



    /**
     * Ejemplo de creacion de un FLux a partir de un Iterable
     * Imprime una lista de nombres.
     */
    private static void createFluxFromIterable() {
        System.out.println("=====================================");
        System.out.println("Create Flux From Iterable Example");
        Flux<String> names = Flux.fromIterable(
                java.util.List.of("Andres", "John", "Jane", "Doe")
        );

        names.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }


    /**
     * Ejemplo de creacion de un FLux con rango
     * Imprime numeros del 1 al 10.
     */
    private static void createFluxFromRange() {
        System.out.println("=====================================");
        System.out.println("Create Flux From Range Example");
        Flux<Integer> numbers = Flux.range(1, 10);

        numbers.subscribe(
                number -> System.out.println(number),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de creacion de un FLux en intervalos
     * Imprime numeros del 0 al 4, uno por segundo.
     * @throws InterruptedException
     */
    public static void createFluxFromInterval() throws InterruptedException {
        System.out.println("=====================================");
        System.out.println("Create Flux From Interval Example");
        Flux<Long> interval = Flux.interval(java.time.Duration.ofSeconds(1))
                .take(5); // Limita a 5 emisiones

        interval.subscribe(
                number -> System.out.println(number),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        // Espera para que el programa no termine inmediatamente
        Thread.sleep(6000);
        System.out.println("=====================================");
    }

    /**
     * Ejemplo de creacion de un FLux a partir de un Map
     * Imprime una lista de nombres (values del Map).
     */
    private static void createFluxFromMap() {
        System.out.println("=====================================");
        System.out.println("Create Flux From Map Example");
        java.util.Map<Integer, String> namesMap = new java.util.HashMap<>();
        namesMap.put(1, "Andres");
        namesMap.put(2, "John");
        namesMap.put(3, "Jane");
        namesMap.put(4, "Doe");

        Flux<String> names = Flux.fromIterable(namesMap.values());

        names.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }
    /**
     * Ejempo de creacion de un FLux en intervalor con rango
     * Imprime numeros del 10 al 14, uno por segundo.
     *
     * @throws InterruptedException
     */
    public static void createFluxFromIntervalRange() throws InterruptedException {
        System.out.println("=====================================");
        System.out.println("Create Flux From Interval Range Example");
        Flux<Long> intervalRange = Flux.interval(java.time.Duration.ofSeconds(1))
                .take(5) // Limita a 5 emisiones
                .map(i -> i + 10); // Comienza desde 10

        intervalRange.subscribe(
                number -> System.out.println(number),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        // Espera para que el programa no termine inmediatamente
        Thread.sleep(6000);
        System.out.println("=====================================");
    }

    /**
     * Ejemplo de creacion de un FLux a partir de un Array
     * Imprime una lista de nombres.
     */
    private static void createFluxFormArray() {
        System.out.println("=====================================");
        System.out.println("Create Flux From Array Example");
        String[] namesArray = new String[]{"Andres", "John", "Jane", "Doe"};
        Flux<String> names = Flux.fromArray(namesArray);

        names.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de creacion de un FLux a partir de un Stream
     * Imprime una lista de nombres.
     */
    private static void createFluxFromStream() {
        System.out.println("=====================================");
        System.out.println("Create Flux From Stream Example");
        Flux<String> names = Flux.fromStream(
                java.util.stream.Stream.of("Andres", "John", "Jane", "Doe")
        );

        names.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de inmutabilidad en un observable
     */
    private static void immutabilityExample() {
        System.out.println("=====================================");
        System.out.println("Inmutabilidad Example");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");

        // Esto no modifica el Flux original, sino que crea uno nuevo
        names.map(String::toUpperCase)
                .subscribe(
                        name -> System.out.println(name),
                        error -> System.err.println(error),
                        () -> System.out.println("Completed")
                );
        // El Flux original sigue igual, iprimbe los nombres en su forma original
        names.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de uso del operador filter en un Flux.
     * Filtra los nombres que tienen más de 4 caracteres.
     */
    private static void filterExample() {
        System.out.println("=====================================");
        System.out.println("Filter Example");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");

        names.filter(name -> name.length() > 4)
                .subscribe(
                        name -> System.out.println(name),
                        error -> System.err.println(error),
                        () -> System.out.println("Completed")
                );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de uso del operador map en un Flux.
     * Ejemplo 01 -> Convierte una cadena de texto en un objeto UserDto(nombre, apellido).
     * Ejemplo 02 -> Convierte una cadena de texto en un objeto UserDto(nombre, apellido) usando split.
     */
    private static void mapExample() {
        System.out.println("=====================================");
        System.out.println("Map Example");

        Flux<UserDto> users = Flux.just("Andres Ruiz", "John Smith", "Jane Sanchez", "Doe Ramirez")
                .map(name -> {
                    String[] parts = name.split(" ");
                    return new UserDto(parts[0], parts[1]);
                });

        users.subscribe(
                user -> System.out.println(user),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de uso del operador flatMap en un Flux.
     * Convierte una cadena de texto en un objeto UserDto(nombre, apellido) usando un switch.
     */
    private static void flatMapExample() {
        System.out.println("=====================================");
        System.out.println("FlatMap Example 01");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");

        Flux<UserDto> users = names.flatMap(name -> {
            // Usamos un switch para asignar un apellido basado en el nombre
            String lastname = switch (name) {
                case "Andres" -> "Ruiz";
                case "John" -> "Smith";
                case "Jane" -> "Sanchez";
                case "Doe" -> "Ramirez";
                default -> "Unknown";
            };
            // Creamos un nuevo UserDto y lo devolvemos como un Flux
            return Flux.just(new UserDto(name, lastname));
        });

        users.subscribe(
                user -> System.out.println(user),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
        System.out.println("FlatMap Example 02");
        Flux<String> namesFull = Flux.just("Andres Ruiz", "John Smith", "Jane Sanchez", "Doe Ramirez")
                .flatMap(name -> Flux.fromArray(name.split("")));

        namesFull.subscribe(
                name -> System.out.println("Name o Apellido: " + name),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    /**
     * Ejemplo de uso del operador concatMap en un Flux.
     * Convierte una cadena de texto en un objeto UserDto(nombre, apellido) usando un switch.
     * A diferencia de flatMap, concatMap mantiene el orden de los elementos.
     * Espera a que finalice la emisión del elemento interno antes de procesar el siguiente.
     * @throws InterruptedException
     */
    private static void concatMapExample() throws InterruptedException {
        System.out.println("=====================================");
        System.out.println("ConcatMap Example");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
                // Simula una fuente de datos más lenta si no imprimira todos los elementos ya que no hay solapamiento temporal en la emision del primero
                .delayElements(Duration.ofMillis(200)); // emite cada 200 ms;

        Flux<UserDto> users = names.concatMap(name -> {
            // Usamos un switch para asignar un apellido basado en el nombre
            String lastname = switch (name) {
                case "Andres" -> "Ruiz";
                case "John" -> "Smith";
                case "Jane" -> "Sanchez";
                case "Doe" -> "Ramirez";
                default -> "Unknown";
            };
            // Creamos un nuevo UserDto y lo devolvemos como un Flux
            return Flux.just(new UserDto(name, lastname))
                    .delayElements(Duration.ofMillis(300)); // Simula una operación más lenta;
        });

        users.subscribe(
                user -> System.out.println(user),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        users.blockLast(); // Espera a que termine la emisión de todos los elementos
        Thread.sleep(100); // Espera un poco más para asegurar que se impriman todos los elementos
        System.out.println("=====================================");
    }

    /**
     * Ejemplo de uso del operador switchMap en un Flux.
     * Convierte una cadena de texto en un objeto UserDto(nombre, apellido) usando un switch.
     * A diferencia de flatMap, switchMap cancela la suscripción anterior si llega un nuevo elemento.
     */
    private static void switchMapExmample() throws InterruptedException {
        System.out.println("=====================================");
        System.out.println("SwitchMap Example 01");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
                // Simula una fuente de datos más lenta si no imprimira todos los elementos ya que no hay solapamiento temporal en la emision del primero
                .delayElements(Duration.ofMillis(200)); // emite cada 200 ms;

        Flux<UserDto> users = names.switchMap(name -> {
            // Usamos un switch para asignar un apellido basado en el nombre
            String lastname = switch (name) {
                case "Andres" -> "Ruiz";
                case "John" -> "Smith";
                case "Jane" -> "Sanchez";
                case "Doe" -> "Ramirez";
                default -> "Unknown";
            };
            // Creamos un nuevo UserDto y lo devolvemos como un Flux
            return Flux.just(new UserDto(name, lastname))
                    .delayElements(Duration.ofMillis(300)); // Simula una operación más lenta;
        });
        users.subscribe(
                user -> System.out.println(user),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        users.blockLast(); // Espera a que termine la emisión de todos los elementos
        Thread.sleep(100); // Espera un poco más para asegurar que se impriman todos los elementos

        System.out.println("=====================================");
        System.out.println("SwitchMap Example 02");

        Mono<String> emptyMono = Mono.empty();
        emptyMono
                .switchIfEmpty(Mono.just("Default Name"))
                .subscribe(
                        name -> System.out.println("Name: " + name),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("Completed")
                );
        System.out.println("=====================================");
    }

    /**
     * Ejemplo de uso del operador groupBy en un Flux.
     * Agrupa los nombres por su primera letra.
     * Cada grupo es un GroupedFlux que contiene los nombres que comienzan con la misma letra.
     * Luego, cada grupo se convierte en una entrada de mapa (clave, lista de nombres) y se aplana en un solo Flux.
     */
    private static void groupByExample() {
        System.out.println("=====================================");
        System.out.println("GroupBy Example 01");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe", "Alice", "Bob", "Charlie", "David");

        // Agrupa los nombres por su primera letra
        Flux<Map.Entry<String,List<String>>> namesGroupByInitialName = names.groupBy(name -> name.substring(0, 1)) // Crea un Flux<GroupedFlux<String,String>>
                // Cada GroupFlux contiente los nombres que comienzan con la misma letra, y con `map` podemos transformar cada grupo
                .map(group ->
                        group.collectList()
                                .map(list -> Map.entry( group.key(), list)) // Devuelve un Mono con la entrada (clave, lista de nombres)
                ) //Flux<Mono<Map.Entry<String,List<String>>>>
                .concatMap(mono -> mono); // Aplana el Flux<Mono<...>> a Flux<...>

        namesGroupByInitialName.subscribe(
                //Flux<Mono<Map.Entry<String,List<String>>>>
//                entryMono -> entryMono.subscribe(
//                        entry -> System.out.println("Group: " + entry.getKey() + " -> " + entry.getValue()),
//                        error -> System.err.println(error),
//                        () -> System.out.println("Group Completed")
//                ),
                //Flux<Map.Entry<String,List<String>>>
                entry -> System.out.println("Group: " + entry.getKey() + " -> " + entry.getValue()),
                // Common
                error -> System.err.println(error),
                () -> System.out.println("All Groups Completed")
        );

        System.out.println("=====================================");
        System.out.println("GroupBy Example 02");
        Flux<String> nameFull = Flux.just("Andres Ruiz", "John Smith", "Jane Ruiz", "Doe Ruiz", "Alice Ramirez", "Bob Smith", "Charlie", "David");

        // Agrupa los nombres por su apellido
        Flux<List<String>> namesGroupByLastName = nameFull.groupBy(name -> {
            String[] parts = name.split(" ");
            return (parts.length > 1) ? parts[1] : "Uknown"; // Si no tiene apellido, lo agrupa en "Unknown"
        }).concatMap( group ->
                group.collectList()
        );

        namesGroupByLastName.subscribe(
                list -> System.out.println("Group: " + list),
                error -> System.err.println(error),
                () -> System.out.println("All Groups Completed")
        );
    }


    private static void reduceExample() {
        System.out.println("=====================================");
        System.out.println("Reduce Example 01");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");

        Mono<String> reduced = names.reduce((name1, name2) -> name1 + " " + name2);

        reduced.subscribe(
                result -> System.out.println("Reduced: " + result),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");

        Flux<BigDecimal> premium = Flux.just(BigDecimal.valueOf(3), BigDecimal.valueOf(5), BigDecimal.valueOf(7), BigDecimal.valueOf(9));

        Mono<BigDecimal> totalPremium = premium.reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("=====================================");
        System.out.println("Reduce Example 03");
        Flux<BigDecimal> taxes = Flux.just(BigDecimal.valueOf(0.3), BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.7), BigDecimal.valueOf(0.9));
        Mono<BigDecimal> totalPremiumWithTaxes = premium
                // por cada prima, calcula el total con impuestos y se suma los resultados de esa prima, y se emite
                // ejem: (100×1.003) + (100×1.005) + (100×1.007) + (100×1.009) + ... = 403.4
                .flatMap(p -> taxes
                        .map(t-> BigDecimal.ONE.add(t.divide(BigDecimal.valueOf(100) )).multiply(p) )
                        .reduce(BigDecimal.ZERO,BigDecimal::add)
                )
                // Por cada emisión de prima con impuestos, se suma para obtneer el total de todas las primas con impuestos
                // ejem: (403.4) + (806.8) + (1210.2) + ... = 2420.4
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalPremiumWithTaxes.subscribe(
                total -> System.out.println("Total Premium + Taxes: " + total),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );
        // Nota: Si el impeusto es uno por prima, entonces se puede usar zipWith (si son del mismo tamaño) o flatMap con un indexador
        System.out.println("=====================================");
    }

    private static void scanExample(){
        System.out.println("=====================================");
        System.out.println("Scan Example with numbers");
        Flux<Integer> numbers = Flux.range(1, 5);

        Flux<Integer> add = numbers.scan(   0, (accumulator, current) -> accumulator + current );

        add.subscribe(
                result -> System.out.println("Scan result: " + result),
                System.err::println,
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
        System.out.println("Scan Example with string");
        Flux<String> names = Flux.just("A", "B", "C", "D");

        Flux<String> addString = names.scan(   (a,b) -> a + b ); // CONCATENA

        addString.subscribe(
                result -> System.out.println("Scan result: " + result),
                System.err::println,
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }
    private static void zipWithExample() {
        System.out.println("=====================================");
        System.out.println("ZipWith Example");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");
        Flux<String> lastnames = Flux.just("Ruiz", "Sanchez", "Smith", "Ramirez");

        Flux<UserDto> users = names
                .zipWith(lastnames, (name, lastname) -> new UserDto(name, lastname));

        users.subscribe(
                user -> System.out.println(user),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
        System.out.println("ZipWith Example 02");

        Flux<BigDecimal> premium = Flux.just(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(300)
        );

        Flux<BigDecimal> taxes = Flux.empty(); // ❌ vacío para probar

        premium
                .zipWith(taxes, (p, t) ->
                        p.multiply(BigDecimal.ONE.add(t.divide(BigDecimal.valueOf(100))))
                )
                .defaultIfEmpty(BigDecimal.ZERO) // 👉 emite este valor si el zip no emite nada
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subscribe(
                        total -> System.out.println("Total Premium + Taxes: " + total),
                        err -> System.err.println(err),
                        () -> System.out.println("Completed")
                );

        System.out.println("=====================================");
    }

    public static void combineLatestExample(){
        System.out.println("=====================================");
        System.out.println("CombineLatest Example (problematico, con la sincronización de emisiones)");

        Flux<BigDecimal> premium = Flux.just(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(300)
        );

        Flux<BigDecimal> taxes = Flux.just(
                BigDecimal.valueOf(0.3),
                BigDecimal.valueOf(0.4),
                BigDecimal.valueOf(0.015),
                BigDecimal.valueOf(0.9)
        );

        Flux<BigDecimal> premiumWithTaxes = Flux.combineLatest(
                premium,
                taxes,
                (p, t) -> p.multiply(BigDecimal.ONE.add(t.divide(BigDecimal.valueOf(100))))
        );//.doOnNext(item -> System.out.println("Intermediate Premium + Tax: " + item));

        // con combineLatest, se emite un valor cada vez que uno de los flujos emite un valor,
        // usando el último valor emitido del otro flujo conocido
        /**
         * Emisiones:
         * premium: 100, 200, 300
         * taxes: 0.3, 0.4, 0.015, 0.9
         * Emisiones combinadas:
         * 1. (100, 0.3) -> 100 * 1.003 = 100.3
         * 3. (200, 0.4) -> 200 * 1.004 = 200.8
         * 4. (300, 0.4) -> 300 * 1.004 = 301.2
         * 5. (300, 0.9) -> 300 * 1.009 = 302.7     << Utiliza el último premium conocido (300)
         *
         * Esto sería en el mundo ideal, pero se producen problmeas de sincronización, y es "primas" puede emitir más rápido que "impuestos"
         * y por lo tanto, se suma el útilimo impuesto conocido a cada prima emitida.
         *
         * Es decir, tendirmaos:
         * 1. (300, 0.3) -> 300 * 1.003 = 300.900
         * 3. (300, 0.4) -> 300 * 1.004 = 301.200
         * 4. (300, 0.4) -> 300 * 1.004 = 300.04500
         * 5. (300, 0.9) -> 300 * 1.009 = 302.700
         */
        premiumWithTaxes.subscribe(
                total -> System.out.println("Premium + Taxes: " + total),
                err -> System.err.println(err),
                () -> System.out.println("Completed")
        );
        System.out.println("=====================================");

        System.out.println("CombineLatest Example - Synced (solución alternativa usando zip)");

        // Asegura, pero limita el tamaño del flujo de impuestos al mismo tamaño que el de primas
        Flux<BigDecimal> synced = Flux.zip(
                premium,
                taxes,//.take(premium.count().block()), // limitar taxes al mismo tamaño
                (p, t) -> p.multiply(BigDecimal.ONE.add(t.divide(BigDecimal.valueOf(100))))
        ); // zip y zipWith emiten solo cuando ambos flujos tienen un valor disponible, por lo que no hay problemas de sincronización, y son similares en este caso

        synced.subscribe(
                total -> System.out.println("Synced Premium + Taxes: " + total),
                err -> System.err.println(err),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");

        System.out.println("CombineLatest Example - Synced with Default (solución alternativa usando withLatestFrom y defaultIfEmpty)");

        // Ponemos el de menos elementos antes, y se comina con el de mayor elementos
        Flux<BigDecimal> syncedWithDefault = premium.withLatestFrom(
                taxes.defaultIfEmpty(BigDecimal.ZERO), // si taxes está vacío, usa 0
                (p, t) -> p.multiply(BigDecimal.ONE.add(t.divide(BigDecimal.valueOf(100))))
        );

        syncedWithDefault.subscribe(
                total -> System.out.println("Synced with Default Premium + Taxes: " + total),
                err -> System.err.println(err),
                () -> System.out.println("Completed")
        );

        // Poenmos el de mayor elementos antes, y se comina con el de menor elementos (este se es real)
        Flux<BigDecimal> syncedWithDefaultInverse = taxes.withLatestFrom(
                premium.defaultIfEmpty(BigDecimal.ZERO), // si taxes está vacío, usa 0
                (p, t) -> p.multiply(BigDecimal.ONE.add(t.divide(BigDecimal.valueOf(100))))
        );

        syncedWithDefaultInverse.subscribe(
                total -> System.out.println("Synced with Default Inverse Premium + Taxes: " + total),
                err -> System.err.println(err),
                () -> System.out.println("Completed")
        );
        System.out.println("=====================================");

    }

    private static void zipWithTakeExample(){
        System.out.println("=====================================");
        System.out.println("ZipWith Take Example");

        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");
        Flux<String> lastnames = Flux.just("Ruiz", "Sanchez");

        names.zipWith(lastnames, (name, lastname) -> new UserDto(name, lastname))
                .take(2) // Limita la cantidad de elementos que emite un Flux
                .subscribe(
                        user -> System.out.println(user),
                        error -> System.err.println(error),
                        () -> System.out.println("Completed")
                );

        System.out.println("=====================================");
    }

    private static void zipWithRangeExample(){
        System.out.println("=====================================");
        System.out.println("ZipWith Range Example");

        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");
        Flux<Integer> numbers = Flux.range(1, 10);

        names.zipWith(numbers, (name, number) -> new UserDto(name, "User#" + number))
                .subscribe(
                        user -> System.out.println(user),
                        error -> System.err.println(error),
                        () -> System.out.println("Completed")
                );

        System.out.println("=====================================");

        System.out.println("With Range Example 02");

        Flux<String> fruits = Flux.just("Apple", "Banana", "Orange", "Grapes");
        Flux<String> colors = Flux.just("Red", "Yellow", "Orange", "Purple", "Green", "Blue");

        Flux.zip(
                fruits,
                colors,
                (fruit, color) -> fruit + " is " + color
        ).skip(2) // Salta los primeros 2 elementos
         .take(3) // Toma los siguientes 3 elementos
         .subscribe(
                item -> System.out.println(item),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }


    private static void delayElementsExample(){
        System.out.println("=====================================");
        System.out.println("Delay Elements Example");

        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
                .delayElements(Duration.ofSeconds(1)); // Retrasa la emisión de cada elemento en 1 segundo

        names.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        // Espera para que el programa no termine inmediatamente
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("=====================================");
    }

    private static void backPressureExample(){
        System.out.println("=====================================");
        System.out.println("BackPressure Example");

        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                        .onBackpressureBuffer(20); // Buffer de 20 elementos

        interval.subscribe(
                item -> System.out.println("Received: " + item),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
    }

    private void convertFluxToMonoExample() {
        System.out.println("=====================================");
        System.out.println("Convert Flux to Mono Example");
        Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe");

        Mono<List<String>> namesListMono = names.collectList(); // Convierte el Flux en un Mono que emite una lista de nombres
        namesListMono.subscribe(
                list -> System.out.println("Names List: " + list),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );
    }

    private void createExample() {
        System.out.println("=====================================");
        System.out.println("Create Example");

        // sink es el emisor del Flux, permite emitir elementos, errores y completar la secuencia
        // Flux.create permite crear un Flux de manera programática, con control total sobre la emisión de elementos, ya sea con sink, como en este caso, o con otros mecanismos como callbacks, listeners, etc.
        Flux<String> customFlux = Flux.create(sink -> {
            sink.next("Andres");
            sink.next("John");
            sink.next("Jane");
            sink.next("Doe");
            sink.complete();
        });

        customFlux.subscribe(
                name -> System.out.println(name),
                error -> System.err.println(error),
                () -> System.out.println("Completed")
        );

        System.out.println("=====================================");
        System.out.println("Create Example with Error");
        // Ejemplo con error
        Flux<String> customFluxWithError = Flux.create(sink -> {
            sink.next("Andres");
            sink.next("John");
            sink.error(new RuntimeException("Custom error occurred"));
        });
        var y = customFluxWithError.subscribe(
                name -> System.out.println(name),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );

        try{
            y.wait();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("=====================================");
        System.out.println("Create Example with Future");
        // Ejemplo con Future
        java.util.concurrent.CompletableFuture<String> future = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // Simula una operación asincrónica
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Result from Future";
        });

        Flux<String> customFluxFromFuture = Flux.create(sink -> {
            future.whenComplete((result, error) -> {
                if (error != null) {
                    sink.error(error);
                } else {
                    sink.next(result);
                    sink.complete();
                }
            });
        });

        var x = customFluxFromFuture.subscribe(
                name -> System.out.println(name),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );

        try{
            x.wait();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("=====================================");
    }
}
