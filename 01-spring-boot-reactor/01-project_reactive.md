# Project Reactive

[Web Oficial](https://projectreactor.io/)
[Guía de Refenica](https://projectreactor.io/docs/core/release/reference/aboutDoc.html)

--- 
# Introducción
Resumen práctico sobre los conceptos y operadores más usados de Project Reactor, enfocado a Spring WebFlux.

--- 
# Módulos de Project Reactor

| Módulo              | Descripción breve                                                                                | Uso típico                                                           | Ejemplo clave                                                               |
| ------------------- | ------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------- | --------------------------------------------------------------------------- |
| **reactor-core**    | Núcleo del framework: define `Flux` (0..N) y `Mono` (0..1), operadores (`map`, `flatMap`, etc.). | Programación reactiva en Spring WebFlux, R2DBC, Reactor Netty, etc.  | `Flux.just("A","B").map(String::toLowerCase)`                               |
| **reactor-test**    | Librería para pruebas unitarias de flujos reactivos.                                             | Usar `StepVerifier` para validar la secuencia de un `Flux` o `Mono`. | `StepVerifier.create(flux).expectNext("A").verifyComplete()`                |
| **reactor-extra**   | Operadores y utilidades extra que no están en el core.                                           | `retryBackoff`, `Tuple2` y otros helpers.                            | `Flux.error(new RuntimeException()).retryBackoff(3, Duration.ofSeconds(1))` |
| **reactor-adapter** | Adaptadores entre Reactor y otros frameworks reactivos.                                          | Convertir RxJava ↔ Reactor, CompletableFuture ↔ Mono.                | `RxJava3Adapter.observableToFlux(rxObs)`                                    |
| **reactor-netty**   | Cliente/servidor HTTP y TCP no bloqueante sobre Netty.                                           | Spring WebFlux en modo server o WebClient.                           | `HttpClient.create().get().uri("/hello")`                                   |
| **reactor-kafka**   | Cliente Kafka basado en Reactor.                                                                 | Consumir y producir mensajes en Kafka de manera reactiva.            | `Receiver.create(...).receive().flatMap(...)`                               |
| **reactor-pool**    | Manejo de pools de recursos reactivos.                                                           | Reuso de conexiones DB, sockets, etc.                                | `PoolBuilder.from(...)`                                                     |

Cuando usas **Spring Boot con WebFlux**, no necesitas traer todos los módulos de Project Reactor manualmente, porque **Spring WebFlux ya integra los esenciales**.

Aquí te detallo los que entran en juego:

| Incluido en WebFlux | Módulo Reactor                                       | ¿Por qué lo integra?                                                                                                             |
| ------------------- | ---------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| ✅                   | **reactor-core**                                     | Es la base de todo: `Flux`, `Mono` y operadores son el corazón de WebFlux.                                                       |
| ✅                   | **reactor-test** (opcional, en `testImplementation`) | Se incluye como dependencia de prueba para poder usar `StepVerifier` en tests.                                                   |
| ⚠️ (opcional)       | **reactor-extra**                                    | No está incluido por defecto, pero puedes añadirlo si necesitas operadores como `retryBackoff`.                                  |
| ⚠️ (opcional)       | **reactor-adapter**                                  | Incluido parcialmente en WebFlux para interoperar con RxJava y `CompletableFuture`. Spring lo usa bajo el capó para adaptadores. |
| ✅                   | **reactor-netty**                                    | Es el runtime por defecto para el servidor y cliente HTTP (a menos que configures otro motor como Jetty o Undertow).             |

--- 
# Reactor + Spring WebFlux
- Spring WebFlux ya integra `reactor-core` y `reactor-netty`.
- `reactor-test` se añade en tests.
- Añade `reactor-extra` o `reactor-adapter` solo si los necesitas.

--- 
# Instalación (Maven)

En el archivo `pom.xml` agregar las siguientes dependencias si no usas Spring Boot:

```xml
<dependencyManagement> 
    <dependencies>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-bom</artifactId>
            <version>2024.0.6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
> Nota: Si se usa Spring Boot, no es necesario agregar el `reactor-bom`, ya que Spring Boot lo incluye por defecto.

Añadir la dependencia de Reactor Core:

```xml
<dependencies>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-core</artifactId> 
        
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId> 
        <scope>test</scope>
    </dependency>
</dependencies>
```

> Nota: 
> - Reactor Test es una dependencia para realizar pruebas unitarias con Reactor y es opcional

--- 
# Crear un Observable + Observer
Un observable emite valores a sus subcriptores, y este puede realizar o no operaciones intermedias:

```java
import reactor.core.publisher.Flux;

Flux<String> names = Flux.just("Andres","Juan","Pedro"); // Publiser
names.subscribe(); // Observer
```
(¡) **¡Importate!** Si no hay una subcripción al observable, este no emite
--- 
# Subscribirse al observable

Esepra a las notificaciones del observable, puedo o no realizar alguna oepraicón

```java
// Observable (emisor de datos)
Flux<String> names = Flux.just("Andres","Juan","Pedro");

// subscibe, hace algo cuando emite
// names.subscribe(); 
names.subscribe(System.out::println);
```
Ejemplo con operaciones
```java
Logger log = LoggerFactory.getLogger(ProjectReacto02CrearObservable.class);

// Observable (emisor de datos)
Flux<String> names = Flux.just("Andres","Juan","Pedro")
    //.filter(element->!element.startsWith("A")); // El orden importa
    // cuando emite imprime
    .doOnNext(System.out::println)
    // filtra
    .filter(element->!element.startsWith("A"));

// subscibe, hace algo cuando emite
names.subscribe(log::info);
```
--- 
# Manejo de errores en un Observable
Un observable puede lanzar una excepción.
Un observer o subcriptor puede capturar la excepción y relaizar una operación.

```java
private static final Logger log = LoggerFactory.getLogger(ProjectReacto03CrearObservable.class);

// Observable (emisor de datos)
Flux<String> names = Flux.just("Andres","Juan","Pedro","","Maria")
    // Simula un error
    .doOnNext( e -> {
        if( e.isEmpty() ){ // Interrumpe el flujo, no se emiete los siguientes
            throw new RuntimeException("Nombres no pueden ser vacíos");
        }else{
            System.out.println(e);
        }
    }
);

// subscibe, hace algo cuando emite
names.subscribe(
    name -> log.info(name),
    error -> log.error(error.getMessage())
);
```

> Nota: Tambien se puede manejar el error con el operador `onErrorResume` o `onErrorReturn` en el observable.

--- 
# Evento `onComplete`
Cuando finaliza un observable correamente, se ejecuta el `onComplete`, el cual, el observer o subcriptor puede realizar alguna tarea.

```java
private static final Logger log = LoggerFactory.getLogger(ProjectReacto03CrearObservable.class);

// Observable (emisor de datos)
Flux<String> names = Flux.just("Andres","Juan","Pedro","","Maria")
        // Simula un error
        .doOnNext( e -> {
                    if( e.isEmpty() ){ // Interrumpe el flujo, no se emiete los siguientes
                        throw new RuntimeException("Nombres no pueden ser vacíos");
                    }else{
                        System.out.println(e);
                    }
                }
        );
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
```
--- 
# Inmutabilidad de un Observable

Un observable es inmutable, es decir, cada vez que se aplica un operador, se crea un nuevo observable.

```java
// Ejemplo 01 de inmutabilidad
Flux<String> names = Flux.just("Andres","Juan","Pedro");
names.map(String::toUpperCase); // No cambia el observable original, si no crea uno nuevo
// Flux<String> namesUpper = names.map(String::toUpperCase); // Crea un nuevo observable
names.subscribe(System.out::println); // Imprime los nombres originales, no en mayúsculas que ese es otro observable


// Ejemplo 02 de inmutabilidad
Flux<String> names = Flux.just("Andres","Juan","Pedro");
Flux<String> namesUpper = names.map(String::toUpperCase); // Crea un nuevo observable
namesUpper.subscribe(System.out::println); // Imprime los nombres en mayúsculas
```
## Observables en Project Reactor `Mono` & `Flux`.
En el mundo de Spring WebFlux (y del Project Reactor, que es su motor reactivo), todo gira en torno a dos tipos principales:

* `Mono<T>`: Representa **una secuencia reactiva que emite como máximo un solo valor, ninguno o un error**.

> Ejemplo:
> ```java
> Mono<String> mono = Mono.just("Hola Mundo"); // Emite un único valor 'Hola Mundo'
> Mono.empty(); // No emite nada, pero completa el flujo
> Mono.error(new RuntimeException("Error!")); // Emite un error
> ```
* `Flux<T>`: Representa **una secuencia reactiva que puede emitir cero o más valores, y luego completa o emite un error**.

> Ejemplo:
> ```java
> Flux<Integer> numeros = Flux.just(1, 2, 3, 4, 5).interval(Duration.ofSeconds(1)).take(5); // Emite 1..5 cada segundo, sin bloqueo del hilo prinicpla
> ```

Comparativa:

| Tipo      | Emite           | Comparable con                | Ejemplo             |
| --------- | --------------- | ----------------------------- | ------------------- |
| `Mono<T>` | 0 o 1 elemento  | `Optional<T>` o `Future<T>`   | `Mono.just("Hola")` |
| `Flux<T>` | 0 a N elementos | `Stream<T>` o `Observable<T>` | `Flux.just(1,2,3)`  |

Sus casos de usso mas frecuentes en Spring son:
* Usa Mono cuando devuelves un solo resultado (como un findById()).
* Usa Flux cuando devuelves una lista o un stream (como un findAll()).

--- 

# Crear un Flux o Mono (_equivalente a un Observable o Publisher_)
Un observable puede crearese de varias formas, las más comunes son:

| Método                                     | Tipo        | Descripción                                                                |
| ------------------------------------------ | ----------- | -------------------------------------------------------------------------- |
| `just(...)`                                | Mono / Flux | Crea un flujo a partir de valores conocidos (secuencia finita).            |
| `fromIterable(...)`                        | Flux        | Crea un flujo a partir de una lista o `Iterable`.                          |
| `fromArray(...)`                           | Flux        | Crea un flujo a partir de un array.                                        |
| `fromStream(...)`                          | Flux        | Crea un flujo a partir de un `Stream`.                                     |
| `range(inicio, cantidad)`                  | Flux        | Crea un flujo que emite una secuencia de números.                          |
| `interval(Duration)`                       | Flux<Long>  | Crea un flujo que emite valores periódicamente (infinito).                 |
| `create(emitter -> {...})`                 | Mono / Flux | Crea un flujo manualmente mediante callbacks asíncronos.                   |
| `generate(state -> {...})`                 | Flux        | Crea un flujo síncrono generando elementos paso a paso.                    |
| `defer(() -> Flux/Mono)`                   | Mono / Flux | Crea un flujo solo cuando alguien se suscribe (lazy).                      |
| `empty()`                                  | Mono / Flux | Crea un flujo vacío (sin valores).                                         |
| `error(Throwable)`                         | Mono / Flux | Crea un flujo que solo emite un error.                                     |
| `never()`                                  | Mono / Flux | Crea un flujo que nunca emite ni completa.                                 |
| `fromCallable(() -> valor)`                | Mono        | Crea un flujo a partir de una función que devuelve un valor.               |
| `fromFuture(Future)`                       | Mono        | Crea un flujo a partir de un `Future`.                                     |
| `fromPublisher(Publisher)`                 | Mono / Flux | Crea un flujo desde otro `Publisher` (como otro Mono/Flux).                |
| `fromMono(Mono)`                           | Flux        | Convierte un `Mono` en `Flux` (útil para composición).                     |
| `fromFlux(Flux)`                           | Mono        | Convierte un `Flux` en `Mono` (por ejemplo, combinando todos los valores). |
| `fromRunnable(() -> {...})`                | Mono<Void>  | Ejecuta una tarea sin devolver valor.                                      |
| `fromSupplier(() -> valor)`                | Mono        | Crea un flujo a partir de un `Supplier` (lazy).                            |
| `fromCompletableFuture(CompletableFuture)` | Mono        | Crea un flujo a partir de un `CompletableFuture`.                          |


## Crear observable a partir de una lista (o Iterable), un Array y un Stream..

Se puede crear un observable a partir de una lista o array, mediante el método `fromIterable`, `fromArray`, `fromStream`.

```java
import java.util.List;

List<String> namesList = List.of("Andres","Juan","Pedro");
String[] namesArray = new String[]{"Andres","Juan","Pedro"};
Stream<String> namesStream = Stream.of("Andres","Juan","Pedro");  

Flux<String> namesFromList = Flux.fromIterable(namesList);
Flux<String> namesFromArray = Flux.fromArray(namesArray);
Flux<String> namesFromStream = Flux.fromStream(namesStream);

```

# Operadores en un Observable `filter`, `map`, `flatMap`,...

Cuando se emite un valor, se pueden aplicar operadores para _transformar, filtrar, combinar, etc_.

```java
Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
        .map(String::toUpperCase)
        .filter(name -> name.length() >= 4)
        .doOnNext(name -> {
            if (name.equals("JANE")) {
                throw new RuntimeException("Error processing name: " + name);
            }
        })
        // Log each name processed
        //.log()
        // Handle error by providing a default value
        .onErrorResume(e -> {
            System.err.println("Caught error: " + e.getMessage());
            return Mono.just("DEFAULT");
        });

// Subscribe with full consumer (onNext, onError, onComplete)
names.subscribe(
        name ->System.out.println("Name: "+name),
        error ->System.err.println("Error: "+error),
        ()->System.out.println("Completed")
);
```

```bash
Reactive01CrearFlux run
Name: ANDRES
Name: JOHN
Caught error: Error processing name: JANE
Name: DEFAULT
Completed
```
## Operador `filter`
Permite filtrar los datos emitidos por el observable.

```java
Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
        .filter(name -> name.length() >= 4);  

names.subscribe(
    name -> System.out.println("Name: " + name),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

```bash
Name: ANDRES
Name: JOHN
Name: JANE
Completed
```

## Operador `map`
Permite realizar una transformación de los datos emitidos por el observable.

```java
Flux<String> names = Flux.just("Andres", "John", "Jane", "Doe")
        .map(String::toUpperCase);

names.subscribe(
    name -> System.out.println("Name: " + name),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

```bash
Name: ANDRES
Name: JOHN
Name: JANE
Name: DOE
Completed
```

> **Nota**: ¿Que pasa si devuelve un `Flux` o `Mono` en lugar de un valor simple en el operador `map`?
> En ese caso, el resultado será un `Flux<Flux<String>>` o `Flux<Mono<String>>`, es decir, un flujo de flujos, lo cual no es lo que generalmente se desea.
> 
> ```java
> Flux<String> names = Flux.just("Andres", "John");
> Flux<Flux<String>> result = names.map(name -> Flux.just(name.toUpperCase(), name.toLowerCase()));
>```
> 
> Esto sería un Flux dentro de otro Flux:
> ```bash
> Flux[
>   Flux["Andres", "Ruiz"],
>   Flux["John", "Smith"],
>   Flux["Jane", "Sanchez"],
> ]
>```
> Y si se hace print de esta forma:
> ```java
> result.subscribe(
>     flux -> flux.subscribe(
>         name -> System.out.println("Name: " + name)
>     )
> );
> ```
> El resultado sería:
> ```bash
> Name: Andres  
> Name: Ruiz
> Name: John
> //...
> ```
> De lo contrario, si se hace un `System.out.println(flux)`, se vería algo como:
> ```bash
> Flux[Flux[Andres, Ruiz], Flux[John, Smith], Flux[Jane, Sanchez]]
> 
> # ó
> 
> FluxConcatMap...
> ```
> Pero esto no es lo ideal, ya que se tienen flujos anidados.
> Para aplanar estos flujos anidados y obtener un solo flujo con los valores emitidos, se utilizan los operadores `flatMap`, `switchMap` o `concatMap`.


## Operador `flatMap`
Permite transformar los datos emitidos por el observable en otro observable y aplanar los resultados.

```java
Flux<String> names = Flux.just("Andres Ruiz", "John Smith", "Jane Sanchez", "Doe Ramize")
        .flatMap(name -> Flux.fromArray(name.split(" ")));

names.subscribe(
    name -> System.out.println("Name o Apellido: " + name),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

```bash
Name o Apellido: Andres
Name o Apellido: Ruiz
Name o Apellido: John
Name o Apellido: Smitch 
Name o Apellido: Jane
Name o Apellido: Sanchez
Name o Apellido: Doe
Name o Apellido: Ramirez
Completed
```  
## Operador `switchMap`
El operador `switchMap` es similar a `flatMap`, pero si el observable fuente emite un nuevo valor, cancela la suscripción al observable anterior y se suscribe al nuevo. Esto es útil cuando solo te interesa el resultado más reciente.

```java
Flux<String> names = Flux.just("Andres Ruiz", "John Smith", "Jane Sanchez", "Doe Ramize")
        // Simula una fuente de datos más lenta si no imprimira todos los elementos ya que no hay solapamiento temporal en la emision del primero
        .delayElements(Duration.ofMillis(200)) // emite cada 200 ms;
        .switchMap(name -> 
                Flux.fromArray(name.split(" "))
                        // se simula operaion mas lenta en el flujo interno, para que se solapes
                        .delayElements(Duration.ofMillis(300)));
names.subscribe(
    name -> System.out.println("Name o Apellido: " + name),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);

names.blockLast(); // Espera a que termine la emisión de todos los elementos
Thread.sleep(100); // Espera un poco más para asegurar que se impriman todos los elementos
```
**Paso a paso:**

1. `Flux.just(...)` emite los nombres uno por uno.
2. Cada nombre se transforma con `switchMap` → `Flux.fromArray(name.split(" "))`, que crea un nuevo flujo con las palabras del nombre.

    * Ejemplo: `"John Smith"` → `Flux.just("John", "Smith")`
3. **Pero**: `switchMap` se suscribe sólo al *último* flujo que ha llegado.

    * Cuando llega `"John Smith"`, se **cancela** el flujo que estaba emitiendo `"Andres Ruiz"`.
    * Luego, cuando llega `"Jane Sanchez"`, se cancela el de `"John Smith"`, y así sucesivamente.

Al final, **solo el último flujo interno completará sus emisiones**, porque cada vez que aparece uno nuevo, el anterior se corta.

> 📊 Resultado real:
> 
> El `Flux` final solo emitirá las palabras del **último nombre**, es decir:
>
>```
>Doe
>Ramize
>completed
>```
> Siempre y cuaNDO el flujo interno no se complete instantáneamente antes de lleger el siguiente (se solapen), de lo contrario si no se solapan, no se cancelará el anterior y se emitirán todos los valores.

**Si quisieras *mantener todos los valores**: Usarías `flatMap` en lugar de `switchMap`.

```java
Flux<String> names = Flux.just("Andres Ruiz", "John Smith", "Jane Sanchez", "Doe Ramize")
        .flatMap(name -> Flux.fromArray(name.split(" ")));
```

👉 En este caso, **no se cancela ninguno**, y el resultado sería:

```
Andres
Ruiz
John
Smith
Jane
Sanchez
Doe
Ramize
```

## Operador `concatMap`
El operador `concatMap` es similar a `flatMap`, pero garantiza que los observables internos se suscriban y emitan en orden secuencial. Esto significa que espera a que el observable interno actual complete antes de suscribirse al siguiente.

```java
Flux<String> names = Flux.just("Andres Ruiz", "John Smith", "Jane Sanchez", "Doe Ramize")
        // Simula una fuente de datos más lenta si no imprimira todos los elementos ya que no hay solapamiento temporal en la emision del primero
        .delayElements(Duration.ofMillis(200)) // emite cada 200 ms;
        .concatMap(name -> 
                Flux.fromArray(name.split(" "))
                        // se simula operaion mas lenta en el flujo interno, para que se solapes
                        .delayElements(Duration.ofMillis(300)));
names.subscribe(
    name -> System.out.println("Name o Apellido: " + name),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
); 
names.blockLast(); // Espera a que termine la emisión de todos los elementos
Thread.sleep(100); // Espera un poco más para asegurar que se impriman todos los
```

## Resumen de operades Transformación en Reacotr

| Operador      | Entrada            | Devuelve  | Aplana automáticamente        | Se solapan flujos | Cancela anteriores | Orden garantizado | Ideal para                               | Ejemplo / Descripción breve             |
| ------------- | ------------------ | --------- | ----------------------------- | ----------------- | ------------------ | ----------------- | ---------------------------------------- | --------------------------------------- |
| `map()`       | `T → R`            | `Flux<R>` | ❌ No                          | ❌                 | ❌                  | ✅                 | Transformaciones simples y síncronas     | Transforma 1 a 1                        |
| `flatMap()`   | `T → Publisher<R>` | `Flux<R>` | ✅ Sí                          | ✅                 | ❌                  | ❌                 | Flujos anidados o asíncronos             | Procesa varios flujos en paralelo       |
| `concatMap()` | `T → Publisher<R>` | `Flux<R>` | ✅ Sí, pero ordenado           | ❌                 | ❌                  | ✅                 | Mantener orden en emisiones secuenciales | Ejecuta cada flujo uno tras otro        |
| `switchMap()` | `T → Publisher<R>` | `Flux<R>` | ✅ Sí, pero cancela anteriores | ✅                 | ✅                  | ❌                 | Solo el último flujo activo importa      | Cancela los anteriores y sigue el nuevo |

**Resumen visual:**
* 🔹 `map()` → Sincrónico, simple.
* 🔹 `flatMap()` → Asíncrono, mezcla resultados de múltiples flujos.
* 🔹 `concatMap()` → Como `flatMap`, pero en orden y sin solaparse.
* 🔹 `switchMap()` → Solo mantiene el flujo más reciente, cancela los previos.

## Operador `groupBy`
El operador **`groupBy()`** permite **dividir un `Flux` en varios sub-Flux** (grupos), basados en una **clave** que tú defines.
Cada grupo emite los elementos que pertenecen a esa clave, de forma independiente.

* 📘 **Sintaxis básica:**

> ```java
> Flux<GroupedFlux<K, V>> groupBy(Function<V, K> keySelector);
> ```
> Donde:
> * `K` → tipo de la **clave** (por ejemplo, un String, un número, etc.)
> * `V` → tipo de los **elementos** originales del `Flux`
> * Devuelve un **`Flux<GroupedFlux<K, V>>`**, donde cada `GroupedFlux` representa un grupo separado.

### 🚀 Ejemplo práctico

Supongamos que tenemos una lista de usuarios con una propiedad `role` (rol):

```java
record User(String name, String role) {}

Flux<User> users = Flux.just(
    new User("Andres", "ADMIN"),
    new User("John", "USER"),
    new User("Jane", "ADMIN"),
    new User("Doe", "USER"),
    new User("Lucy", "GUEST")
);
```

Queremos **agruparlos por rol**:

```java
users
    .groupBy(User::role) // Agrupa por el campo "role"
    .flatMap(groupedFlux -> groupedFlux
        .collectList()
        .map(list -> Map.entry(groupedFlux.key(), list))
    )
    .subscribe(entry -> {
        System.out.println("Rol: " + entry.getKey());
        entry.getValue().forEach(u -> System.out.println("  - " + u.name()));
    });
```
> 🧠 ¿Qué está pasando aquí?
>
> 1. `groupBy(User::role)` crea un `Flux<GroupedFlux<String, User>>`.
>
>     👉 Cada `GroupedFlux` contiene solo los usuarios con un rol determinado.
>
> 2. `flatMap(...)` permite procesar cada grupo.
>
>   * `groupedFlux.key()` → devuelve la **clave del grupo** (`"ADMIN"`, `"USER"`, `"GUEST"`).
>   * `groupedFlux.collectList()` → recoge todos los elementos del grupo en una lista.
>
> 3. Luego, se imprime cada grupo por separado.

* 🖨️ **Salida esperada:**

```
Rol: ADMIN
  - Andres
  - Jane
Rol: USER
  - John
  - Doe
Rol: GUEST
  - Lucy
```
### ⚙️ Variación: Agrupar por rango numérico

También se puede agrupar por cualquier criterio dinámico.
Por ejemplo, agrupar números en pares e impares:

```java
Flux<Integer> numbers = Flux.range(1, 6);

numbers
    .groupBy(n -> n % 2 == 0 ? "Pares" : "Impares")
    .flatMap(groupedFlux ->
        groupedFlux.collectList()
                   .map(list -> Map.entry(groupedFlux.key(), list))
    )
    .subscribe(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
```

* 🖨️ **Salida:**

```
Impares: [1, 3, 5]
Pares: [2, 4, 6]
```

## ⚠️ Consideraciones importantes

| Concepto                | Descripción                                                                                |
| ----------------------- | ------------------------------------------------------------------------------------------ |
| **Salida de `groupBy`** | Devuelve un `Flux` de `GroupedFlux` (no de listas).                                        |
| **Suscripción**         | Cada `GroupedFlux` necesita suscribirse (directa o indirectamente vía `flatMap`).          |
| **Memoria**             | Cuidado con flujos infinitos → `groupBy()` puede mantener grupos abiertos indefinidamente. |
| **Uso típico**          | Agrupar eventos, logs, mensajes, usuarios, etc. por tipo, categoría o propiedad.           |

---

## Operador `reduce` y `scan`

Tanto **reduce** como **scan** son operadores que acumulan resultados mientras se procesan los elementos de un **Flux**.


### 🧩 ¿Qué es `reduce()` en Reactor?

El operador **`reduce()`** sirve para **acumular (reducir)** todos los elementos de un `Flux` en **un solo valor final**, aplicando una función **binaria** (de dos argumentos).

📘 **Sintaxis básica:**

```java
Mono<T> reduce(BinaryOperator<T> accumulator)
Mono<R> reduce(R seed, BiFunction<R, ? super T, R> accumulator)
```
Dónde:

* 🔹 `reduce(accumulator)` → combina todos los elementos **de izquierda a derecha** usando la función acumuladora.
  Devuelve un **`Mono<T>`**, porque el resultado final es un solo valor.
* 🔹 `reduce(seed, accumulator)` → igual, pero empieza con un **valor inicial (`seed`)**.

Ejemplo:

```java
Flux<Integer> numeros = Flux.just(1, 2, 3, 4, 5);

Mono<Integer> suma = numeros.reduce(0, (acum, valor) -> acum + valor);

suma.subscribe(System.out::println);

// El resultado será: 15
/*
🧠 Explicación:

El flujo tiene 5 elementos.

reduce acumula:
0 + 1 → 1
1 + 2 → 3
3 + 3 → 6
6 + 4 → 10
10 + 5 → 15

Al final emite solo el resultado final (15) como un Mono<Integer>
 */
```

### ¿Qué es el operador `scan()` en Reactor?
El operador **`scan()`** lo que realiza es emitir el **valor acumulado en cada paso**, es decir, emite un nuevo valor cada vez que se procesa un elemento del `Flux`.

Esto permite ver la evolución del valor acumulado a lo largo del tiempo incluyendo el valor inicial (_incluso, si este fuera cero_), es usado para mostrar e l **progreos, historial de acmulación**, o **animaicones en tiempo real**.

```java
Flux<Integer> numeros = Flux.just(1, 2, 3, 4, 5);

Flux<Integer> acumulados = numeros.scan(0, (acum, valor) -> acum + valor);

acumulados.subscribe(System.out::println);

// El resultado será: 15
/*
El resultado será:
0
1
3
6
10
15

🧠 Explicación:

scan emite cada paso intermedio, incluyendo el valor inicial (0).

Al final emite un nuevo Flux<Integer>
 */
```

#### 🚀 Ejemplo 1: Suma de números

```java
Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);

Mono<Integer> sum = numbers.reduce((a, b) -> a + b);

sum.subscribe(result -> System.out.println("Suma total: " + result));
```

🖨️ **Salida:**

```
Suma total: 15
```

📘 **Qué pasó:**

* `reduce()` toma los elementos del `Flux`: 1, 2, 3, 4, 5
* Aplica:

  ```
  (((1 + 2) + 3) + 4) + 5 = 15
  ```
* Devuelve un `Mono<Integer>` con el valor final.

#### 🚀 Ejemplo 2: Usando un valor inicial (`seed`)

```java
Flux<Integer> numbers = Flux.just(1, 2, 3);

Mono<Integer> sum = numbers.reduce(10, (acc, n) -> acc + n);

sum.subscribe(result -> System.out.println("Suma total con seed: " + result));
```

🖨️ **Salida:**

```
Suma total con seed: 16
```

📘 **Qué pasó:**

* Valor inicial = 10
* Cálculo: `10 + 1 + 2 + 3 = 16`

#### 🚀 Ejemplo 3: Concatenar cadenas

```java
Flux<String> names = Flux.just("Andres", "John", "Jane");

names
    .reduce((a, b) -> a + ", " + b)
    .subscribe(result -> System.out.println("Nombres: " + result));
```

🖨️ **Salida:**

```
Nombres: Andres, John, Jane
```

📘 Aquí `reduce()` funciona como un `join` para strings.

#### 🚀 Ejemplo 4: Reducir a un objeto complejo

```java
record User(String name, int age) {}

Flux<User> users = Flux.just(
    new User("Andres", 30),
    new User("John", 25),
    new User("Jane", 28)
);

users
    .reduce(0, (total, user) -> total + user.age())
    .subscribe(result -> System.out.println("Edad total: " + result));
```

🖨️ **Salida:**

```
Edad total: 83
```
#### 🚀 Ejemplo 5: Diferencia entre `scan()` y `reduce()`

```java
Flux<Integer> numbers = Flux.just(1, 2, 3);

System.out.println("🔹 scan():");
numbers.scan((a, b) -> a + b)
       .subscribe(System.out::println);

System.out.println("🔹 reduce():");
numbers.reduce((a, b) -> a + b)
       .subscribe(System.out::println);
```

🖨️ **Salida:**

```
🔹 scan():
1
3
6
🔹 reduce():
6
```

## ✅ Resumen de `reduce()` & `scan()`

| Característica               | Descripción                                                                    |
| ---------------------------- | ------------------------------------------------------------------------------ |
| **Devuelve**                 | `Mono<T>`                                                                      |
| **Emite**                    | Un único valor acumulado                                                       |
| **Usa**                      | Función acumuladora `(acumulador, valorActual)`                                |
| **Ejemplo típico**           | Sumar, concatenar, combinar listas, calcular totales                           |
| **Comparación con `scan()`** | `reduce()` solo emite el resultado final; `scan()` emite todos los intermedios |

### 🧠 Comparación con otros operadores

| Operador   | Tipo de resultado | Emite múltiples valores       | Acumula internamente | Finaliza con un solo valor |
| ---------- | ----------------- | ----------------------------- | -------------------- | -------------------------- |
| `map()`    | `Flux<R>`         | ✅ Sí                          | ❌ No                 | ❌ No                       |
| `scan()`   | `Flux<R>`         | ✅ Sí (emite cada acumulación) | ✅ Sí                 | ❌ No                       |
| `reduce()` | `Mono<R>`         | ❌ No                          | ✅ Sí                 | ✅ Sí                       |

🧩 **Diferencia entre `reduce()` y `scan()`**

La diferencia principal:

| Operador | Qué hace                                                                                   |
| -------- | ------------------------------------------------------------------------------------------ |
| `reduce` | **Acumula todos los valores** y **emite un único resultado final** (como un `fold` en FP). |
| `scan`   | **Emite cada paso intermedio** de la acumulación (como un “reduce paso a paso”).           |


---

## Operador `zipWith`
**zipWith** es un **operador combinador**. Y sirve para:

1. Sincronizar dos flujos (_Flux o Mono_)
2. empaquetar sus valores emitidos en pareS.

> _En otras palabras_: Toma un elemento de cada flujo y los combina en un solo valor, usando una función que tú defines.
> ```plantuml
> +------------------+      +------------------+
> |    Flux<T>       |      |    Flux<U>       |
> +------------------+      +------------------+
>              \                   /
>               \                 /
>               \               /
>               +-------------+
>               |  zipWith()  |
>               +-------------+
>               | Flux<V>     |
>               +-------------+
> ```

Sintaxis básica:

```java
flux1.zipWith(flux2, (valor1, valor2) -> combinación)
```

Ejemplo:
```java
Flux<String> nombres = Flux.just("Ana", "Luis", "Marta");
Flux<Integer> edades = Flux.just(25, 30, 22);

Flux<String> combinados = nombres.zipWith(edades, (nombre, edad) -> nombre + " tiene " + edad + " años");

combinados.subscribe(System.out::println);

/**
 * Salida:
 * Ana tiene 25 años
 * Luis tiene 30 años
 * Marta tiene 22 años
 */
```

### 🧩 Ejemplo prácitico completo

Se tiene un `Flux` de primas (`premium`) y otro de impuestos (`taxes`), y lo que se quiere es obtener la suma total de primas más impuestos.

```java
Flux<BigDecimal> premium = Flux.just(
        BigDecimal.valueOf(10.0),
        BigDecimal.valueOf(20.0),
        BigDecimal.valueOf(30.0),
        BigDecimal.valueOf(40.0)
);

Flux<BigDecimal> taxes = Flux.just(
        BigDecimal.valueOf(0.3),
        BigDecimal.valueOf(0.5),
        BigDecimal.valueOf(0.7),
        BigDecimal.valueOf(0.9)
);

premium
    .zipWith(taxes, (p, t) -> p.add(t)) // 🔹 Empareja elemento a elemento
    .reduce(BigDecimal.ZERO, BigDecimal::add) // 🔹 Suma todos los resultados
    .subscribe(
        total -> System.out.println("Total Premium + Taxes: " + total),
        error -> System.err.println(error),
        () -> System.out.println("Completed")
    );
```


#### ⚙️ Explicación Paso a paso — cómo se comporta `zipWith()`

1. El operador **`zipWith()`** combina **dos `Flux`** *elemento a elemento*, como una cremallera (`zip`), aplicando una función binaria a cada par.

```
Flux A: 10.0, 20.0, 30.0, 40.0
Flux B: 0.3, 0.5, 0.7, 0.9
```

👉 `zipWith` empareja así:

| Índice | premium (p) | taxes (t) | Resultado p.add(t) |
| -----: | ----------: | --------: | -----------------: |
|      0 |        10.0 |       0.3 |               10.3 |
|      1 |        20.0 |       0.5 |               20.5 |
|      2 |        30.0 |       0.7 |               30.7 |
|      3 |        40.0 |       0.9 |               40.9 |

📘 Luego, `zipWith()` devuelve un nuevo `Flux<BigDecimal>` con esos resultados:

```
Flux result = [10.3, 20.5, 30.7, 40.9]
```

---

2. Después entra `reduce()`, con la siguiente línea, para ir acumulando la suma total.

```java
.reduce(BigDecimal.ZERO, BigDecimal::add)
```

```
0 + 10.3 + 20.5 + 30.7 + 40.9 = 102.4
```

3. El Resultado final → `Mono<BigDecimal>` con valor `102.4`

En 🧠 resumen del ejemplo

| Etapa | Operador             | Entrada                                | Salida             | Descripción                                   |
| ----- | -------------------- | -------------------------------------- | ------------------ | --------------------------------------------- |
| 1️⃣   | `zipWith()`          | `Flux<BigDecimal>`, `Flux<BigDecimal>` | `Flux<BigDecimal>` | Combina ambos flujos **índice a índice**      |
| 2️⃣   | `(p, t) -> p.add(t)` | Cada par (p, t)                        | `p + t`            | Suma la prima con su impuesto correspondiente |
| 3️⃣   | `reduce()`           | `Flux<BigDecimal>`                     | `Mono<BigDecimal>` | Suma todos los resultados finales             |

Y como 📊 Diagrama conceptual del ejemplo

```
premium: [10.0] [20.0] [30.0] [40.0]
taxes:   [0.3 ] [0.5 ] [0.7 ] [0.9 ]
           ↓       ↓       ↓       ↓
zipWith → [10.3] [20.5] [30.7] [40.9]
           ↓       ↓       ↓       ↓
reduce → 102.4
```
 
## ⚠️ Notas importantes sobre `zipWith()` (_particularidades_)

* Si los dos `Flux` tienen **diferente longitud**, `zipWith` **se detiene en el más corto**.
  → Ejemplo: si `taxes` tuviera solo 2 elementos, solo emparejaría los dos primeros.

* `zipWith` **no mezcla ni reordena**; siempre va **uno a uno y en orden**.

### ¿Qué ocurre cuando el segundo `Flux` (`taxes`) tiene **menos elementos** que el primero (`premium`).

Veámoslo en un ejemplo práctico

```java
Flux<BigDecimal> premium = Flux.just(
        BigDecimal.valueOf(10.0),
        BigDecimal.valueOf(20.0),
        BigDecimal.valueOf(30.0),
        BigDecimal.valueOf(40.0)
);

Flux<BigDecimal> taxes = Flux.just(
        BigDecimal.valueOf(0.3),
        BigDecimal.valueOf(0.5)
); // 👈 solo 2 elementos

premium
    .zipWith(taxes, (p, t) -> p.add(t))
    .subscribe(
        System.out::println,
        System.err::println,
        () -> System.out.println("Completed")
    );
```

Lo que `zipWith()` hace, es **emparejar los elementos por posición** y **detenerse cuando uno de los dos flujos termina**. Es decir:

| Índice | premium (p) |            taxes (t) | Resultado (p + t) |
| -----: | ----------: | -------------------: | ----------------: |
|      0 |        10.0 |                  0.3 |              10.3 |
|      1 |        20.0 |                  0.5 |              20.5 |
|      2 |        30.0 | ❌ (no hay más taxes) |     🚫 se detiene |

Por tanto, el flujo resultante emite **solo los dos primeros elementos**:

```
10.3
20.5
Completed
```
> ✅ Comportamiento oficial (según Reactor), cuando los flujos tienen diferente longitud:
> 
> `zipWith()` completa **tan pronto como uno de los flujos fuente se completa**.
> Lo que quiere decir, si una fuente se queda sin elementos, **el flujo combinado termina**, aunque la otra todavía tenga más datos pendientes.


### 🧩 Si quisieras un comportamiento diferente en flujos desiguales

Hay otras alternativas si **quieres manejar flujos desiguales**:

| Operador                           | Comportamiento                                                                  | Resultado cuando una fuente es más corta     |
| ---------------------------------- | ------------------------------------------------------------------------------- | -------------------------------------------- |
| `zipWith()`                        | Se detiene en el más corto                                                      | ❌ pierde el resto                            |
| `combineLatest()`                  | Usa el último valor conocido de cada fuente                                     | ✅ mantiene activo hasta que ambas terminen   |
| `zipWithIterable()`                | Puedes pasar un `Iterable` más corto o largo, se comporta igual que `zipWith()` | ❌ se detiene en el más corto                 |
| `zipWith(...).defaultIfEmpty(...)` | Permite manejar flujos vacíos                                                   | Solo útil si alguno está completamente vacío |


### 🧪 Ejemplo con `combineLatest`

Si quisieras que se sigan combinando **hasta que ambos terminen**, puedes usar:

```java
Flux.combineLatest(
    premium,
    taxes,
    (p, t) -> p.add(t)
)
.subscribe(System.out::println);
```

🔹 En este caso, `combineLatest` emitirá **cada vez que uno de los dos flujos emita un nuevo valor**, usando el último valor del otro.
🔹 No se detendrá hasta que **ambos flujos hayan completado**.

> **¡Importante!** El operador `combineLatest` puede emitir más valores que `zipWith`, ya que cada vez que uno de los flujos produce un nuevo valor, este se combina con el último valor conocido del otro flujo.
> Sin embargo, este comportamiento puede generar combinaciones inesperadas si los flujos no están sincronizados correctamente, especialmente cuando uno de ellos emite más rápido que el otro o finaliza antes.
> Por esta razón, cuando se requiere sincronización estricta entre flujos, es preferible utilizar operadores como:
> * zip / zipWith → combina elementos uno a uno en orden.
> * withLatestFrom → emite solo cuando el flujo principal produce un valor, usando el último valor disponible del otro.
> * merge → mezcla emisiones de varios flujos sin combinarlas, útil cuando no se necesita sincronía.
> > Estos operadores limitan las emisiones al tamaño del flujo más pequeño, garantizando una correspondencia controlada entre los elementos y evitando resultados inesperados.

### 🧾 Resumen

| Caso                                                 | `zipWith` comportamiento                  |
| ---------------------------------------------------- | ----------------------------------------- |
| Ambos flujos tienen la misma longitud                | ✅ Empareja todos los elementos            |
| Uno termina antes (por ejemplo, `taxes` más corto)   | ⚠️ Se detiene al completarse el más corto |
| Uno está vacío                                       | 🚫 El resultado está vacío                |
| Necesitas seguir combinando aunque uno sea más corto | 👉 Usa `combineLatest()`                  |


## Operador `zipWithTake` - 'zipWith() + take()'

El operador `zipWithTake` no es un operador nativo de Reactor, pero se puede implementar combinando `zipWith` con `take()`. Este operador permite combinar dos flujos y limitar el número de elementos emitidos al tamaño del flujo más corto.

```java
import reactor.core.publisher.Flux;

Flux<T> flux1;
Flux<U> flux2;

Flux<R> result = flux1.zipWith(flux2, (t, u) -> R).take(Math.min(flux1.count().block(), flux2.count().block()).intValue());

// En una función genérica:
Flux<R> zipWithTake(Flux<T> flux1, Flux<U> flux2, BiFunction<T, U, R> combiner) {
    return flux1.zipWith(flux2, combiner)
                 .take(Math.min(flux1.count().block(), flux2.count().block()).intValue());
}
```

En resumen:

| Concepto        | Qué hace                                                  | Se detiene cuando        | Ejemplo                        |
| --------------- | --------------------------------------------------------- | ------------------------ | ------------------------------ |
| `zipWith()`     | Combina 2 flujos posición a posición                      | Uno de los dos termina   | `flux1.zipWith(flux2)`         |
| `take(n)`       | Toma solo los primeros `n` elementos del flujo resultante | Después de `n` emisiones | `.take(3)`                     |
| “`zipWithTake`” | No existe como método — combinación de ambos              | —                        | `flux1.zipWith(flux2).take(n)` |

---
## Operador `zipWithRange` - 'zipWith() + range()' 
El operador `zipWithRange` no es un operador nativo de Reactor, pero se puede implementar combinando `zipWith` con `range()`. Este operador permite combinar dos flujos y limitar el número de elementos emitidos a un rango específico.

Es util cuando se desea combinar dos flujos pero solo se quiere procesar un subconjunto de los elementos combinados.

También e puede implementar usando `skip()` y `take()` para definir un rango específico de elementos a emitir, una vez se realice la combinación con `zipWitch()`.

```java
import reactor.core.publisher.Flux;
Flux<T> flux1;
Flux<U> flux2;
int start = 2; // índice de inicio
int end = 5;   // índice de fin
Flux<R> result = flux1.zipWith(flux2, (t, u) -> R)
                       .skip(start) // Saltar los primeros 'start' elementos
                       .take(end - start); // Tomar 'end - start' elementos
// En una función genérica:
Flux<R> zipWithRange(Flux<T> flux1, Flux<U> flux2, BiFunction<T, U, R> combiner, int start, int end) {
    return  flux1.zipWith(flux2, combiner)
                 .skip(start)
                 .take(end - start);
}
```

## Operador `dealyElements()`

El operador `delayElements` en Reactor se utiliza para retrasar la emisión de cada elemento en un `Flux` o `Mono` por un período de tiempo específico. Esto es útil para simular latencias, controlar la velocidad de emisión o espaciar eventos en el tiempo.

```java
import reactor.core.publisher.Flux;
import java.time.Duration;  
Flux<String> flux = Flux.just("A", "B", "C", "D")
                        .delayElements(Duration.ofSeconds(1)); // Retrasa cada elemento 1 segundo

flux.subscribe(System.out::println);
```
## Operador `backPressureExample()`
El operador de control de retropresión (backpressure) en Reactor se utiliza para gestionar la velocidad a la que los datos son emitidos por un `Flux` o `Mono`, asegurando que el consumidor no se vea abrumado por una cantidad excesiva de datos en un corto período de tiempo. 

Reactor proporciona varios mecanismos para manejar la retropresión, como `onBackpressureBuffer()`, `onBackpressureDrop()`, y `onBackpressureLatest()`.

```java
import reactor.core.publisher.Flux;
// Buffer de hasta 100 elementos, si se pasa este límite, se aplicará retropresión, es decir, se ralentizará la emisión de datos. 
// Esta estrategia es útil cuando se desea almacenar temporalmente los datos hasta que el consumidor esté listo para procesarlos.
Flux<Integer> fastProducer = Flux.range(1, 1000)
                                         .onBackpressureBuffer(100); 
```

## Operador `createExample()`
El operador `create` en Reactor se utiliza para crear un `Flux` o `Mono` personalizado a partir de un `FluxSink` o `MonoSink`. Esto permite emitir elementos de manera programática, proporcionando un control total sobre la emisión de datos, errores y la finalización del flujo.    

Se puede utilizar para integrar fuentes de datos externas o para crear flujos que no se ajustan a los patrones estándar de Reactor.

```java
import reactor.core.publisher.Flux;
Flux<String> customFlux = Flux.create(sink -> {
    sink.next("Elemento 1");
    sink.next("Elemento 2");
    sink.complete(); // Indica que la emisión ha finalizado
});

customFlux.subscribe(System.out::println);
```

Donde `skin` es un `FluxSink` que permite emitir elementos, errores y completar el flujo.

Por otro lado, también se puede utilizar `Mono.create()` para crear un `Mono` personalizado de manera similar.

En ambos casos, como alternativa a `skin.next()`, se puede utilizar `sink.error()` para emitir un error y `sink.complete()` para indicar que la emisión ha finalizado correctamente.

Así como, otras alternativas, `listeners`, `callbacks`, `event emitters`, etc.
