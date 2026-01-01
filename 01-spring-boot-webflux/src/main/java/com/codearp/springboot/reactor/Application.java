package com.codearp.springboot.reactor;

import com.codearp.springboot.reactor.models.documents.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class Application implements CommandLineRunner {

    private final com.codearp.springboot.reactor.dao.ProductDao productDao;

    // Para borrar la colección al iniciar la aplicación
    private final ReactiveMongoTemplate mongoTemplate;

    @Value("${app.debug:false}")
    private boolean debug;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if( Boolean.FALSE.equals(debug) ) {
            return;
        }

        log.info("Inicializando datos de prueba...");

        mongoTemplate.dropCollection("products").subscribe();

        Flux.just(
                Product.builder().withName("TV Panasonic Pantalla LCD").withPrice(456.89).build(),
                Product.builder().withName("Sony Camara HD Digital").withPrice(177.89).build(),
                Product.builder().withName("Apple iPod").withPrice(46.89).build(),
                Product.builder().withName("Sony Notebook").withPrice(846.89).build(),
                Product.builder().withName("Hewlett Packard Multifuncional").withPrice(200.89).build(),
                Product.builder().withName("Bianchi Bicicleta").withPrice(70.89).build(),
                Product.builder().withName("HP Notebook Omen 17").withPrice(2500.89).build(),
                Product.builder().withName("Mica Cómoda 5 Cajones").withPrice(150.89).build()
        ).flatMap(productDao::save) // FlatMap covierte el Mono<Product> a Product
             .subscribe(product -> {
                    log.info("Producto creado: " + product.getName() + ", Precio: " + product.getPrice());

        });

    }
}
