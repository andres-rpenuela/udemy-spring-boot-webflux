# Programación Reactiva en Java con Spring

La programación reactiva es un paradigma de desarrollo que permite manejar flujos de datos asíncronos y eventos de manera no bloqueante.

En Java y Spring, se utiliza para construir aplicaciones que pueden escalar mejor y responder eficientemente a múltiples solicitudes concurrentes.

## En Java:
e basa en el estándar Reactive Streams, que define cómo los componentes interactúan de forma asíncrona usando operadores como map, filter, etc. Las implementaciones más conocidas son Project Reactor (usado en Spring) y RxJava

## En Spring
pring introduce el módulo WebFlux, que permite crear APIs reactivas usando los tipos Mono (0 o 1 elemento) y Flux (0 a N elementos) de Project Reactor. Esto permite manejar peticiones HTTP, acceso a bases de datos y otros recursos de forma no bloqueante, mejorando el rendimiento en aplicaciones con alta concurrencia.

## Ventajas

* Mejor uso de recursos (threads).
* Escalabilidad.
* Menor latencia en aplicaciones concurrentes.
* Basado en el patron Observer + Patrón Iterator + Pramación funcional 

Ejemplo báisco en **Spring WebFlux**:
```java
@RestController
public class ProductController {
    @GetMapping("/products")
    public Flux<Product> getAll() {
        return Flux.just(
            new Product(1L, "Monitor"),
            new Product(2L, "Teclado")
        );
    }
}
```
> En este ejemplo, el controlador retorna un Flux<Product>, permitiendo enviar los productos de forma reactiva al cliente.

## Patron Observer
El patrón Observer en Java permite que un objeto (Observable) notifique automáticamente a otros objetos (Observers) cuando cambia su estado. Es útil para manejar eventos y flujos de datos, lo que lo hace fundamental en la programación reactiva.

Ejemplo:
```java
// Observable
public class Notificador extends java.util.Observable {
    public void cambioEstado(String mensaje) {
        setChanged();
        notifyObservers(mensaje);
    }
}

// Observer
public class Receptor implements java.util.Observer {
    @Override
    public void update(java.util.Observable o, Object arg) {
        System.out.println("Recibido: " + arg);
    }
}

// Uso
Notificador notificador = new Notificador();
Receptor receptor = new Receptor();
notificador.addObserver(receptor);
notificador.cambioEstado("¡Evento!");
```
**En programaicón reactiva**, el patrón Observer es la base de los flujos reactivos: los Observers se suscriben a un flujo (Observable, Publish, Flux, Mono) y reaccionan a los cambios o eventos emitidos.

```java
Flux<String> flujo = Flux.just("A", "B", "C");
flujo.subscribe(valor -> System.out.println("Recibido: " + valor));
```
> Por tanto:
> - Flux = Observable (emite datos)
> - Suscriptor (lambda en subscribe) = Observer (recibe y reacciona a los datos)

## Project Reactor vs RxJava en Spring

### 🔹 1. Project Reactor (el recomendado en Spring)

* Spring 5 introdujo el stack reactivo con Spring WebFlux, que está basado en Project Reactor (Mono y Flux).
* Reactor es la implementación oficial de Reactive Streams para el ecosistema Spring.
* Todas las librerías modernas de Spring (WebFlux, R2DBC, Spring Cloud Gateway, etc.) trabajan directamente con Reactor.

Ejemplo:
```java
@RestController
public class ProductController {

    @GetMapping("/products")
    public Flux<Product> getAll() {
        return Flux.just(
            new Product(1L, "Monitor"),
            new Product(2L, "Teclado")
        );
    }
}
```

### 🔹 2. RxJava

* RxJava fue muy popular antes de Reactor, especialmente en Android y microservicios Java.
* Spring no lo usa por defecto, pero puedes integrarlo fácilmente.
* Existe RxJava adapter para convertir entre tipos (Observable, Single) y Reactor (Flux, Mono).

Ejemplo con el adaptador `reactor.adapter.rxjava`:

```java
import reactor.adapter.rxjava.RxJava3Adapter;

Observable<String> rxObservable = Observable.just("A", "B", "C");

// Convertir Observable -> Flux
Flux<String> flux = RxJava3Adapter.observableToFlux(rxObservable);
```

### 🔹 3. ¿Cuál usar en Spring?

* ✅ Project Reactor si usas Spring WebFlux, R2DBC o cualquier cosa del stack moderno de Spring.
* ✅ Puedes usar RxJava en tu lógica de negocio si ya tienes librerías que dependen de él, pero normalmente lo adaptas a Reactor para integrarlo con Spring.
* ❌ No se recomienda usar RxJava directamente en los controladores o servicios WebFlux, porque Spring no lo soporta de forma nativa (todo está basado en Mono/Flux).