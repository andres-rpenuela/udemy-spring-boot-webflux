# Project Reactive

[Web Oficial](https://projectreactor.io/)
[Guía de Refenica](https://projectreactor.io/docs/core/release/reference/aboutDoc.html)


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

> 📌 **En resumen**:
> * Siempre vas a tener **reactor-core** y **reactor-netty** cuando usas WebFlux.
> * En **tests** tendrás **reactor-test**.
> * **reactor-extra** y **reactor-adapter** los añades solo si los necesitas explícitamente.

# Instalacion de Maven

En el archivo `pom.xml` agregar las siguientes dependencias:

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

Añadir la dependneica de Reactor Core:

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


# Crear un Observable + Observer
Un boservable emite valores a sus subcriptores, y este puede realizar o no operaciones intermacidas:

```java
import reactor.core.publisher.Flux;

Flux<String> names = Flux.just("Andres","Juan","Pedro"); // Publiser
names.subscribe(); // Observer
```
*Importate* Si no hay una subcripción al observable, este no emite

# Subcibirse al observable

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

# Maneo de errores en un Observable
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