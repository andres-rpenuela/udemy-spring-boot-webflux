package com.codearp.springboot.reactor;

import com.codearp.springboot.reactor.models.documents.Category;
import com.codearp.springboot.reactor.models.documents.Product;
import com.mongodb.client.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class Application implements CommandLineRunner {

    private final com.codearp.springboot.reactor.dao.ProductDao productDao;
    private final com.codearp.springboot.reactor.dao.CategoryDao categoryDao;

    // Para borrar la colección al iniciar la aplicación
    private final ReactiveMongoTemplate mongoTemplate;
    private final MongoClient mongo;

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
        // Borramos la colección antes de insertar los datos
        mongoTemplate.dropCollection("categories").subscribe();
        mongoTemplate.dropCollection("products").subscribe();
        Map<String, Category> categoryMap = new HashMap<>();
        categoryMap.put( Category.CategoryName.ELECTRONICS.name(), Category.builder().name( Category.CategoryName.ELECTRONICS ).build() );
        categoryMap.put( Category.CategoryName.HOME.name(), Category.builder().name( Category.CategoryName.HOME ).build() );
        categoryMap.put( Category.CategoryName.BEAUTY.name(), Category.builder().name( Category.CategoryName.BEAUTY ).build() );
        categoryMap.put( Category.CategoryName.SPORTS.name(), Category.builder().name( Category.CategoryName.SPORTS ).build() );
        categoryMap.put( Category.CategoryName.TOYS.name(), Category.builder().name( Category.CategoryName.TOYS ).build() );
        categoryMap.put( Category.CategoryName.AUTOMOTIVE.name(), Category.builder().name( Category.CategoryName.AUTOMOTIVE ).build() );
        categoryMap.put( Category.CategoryName.FASHION.name(), Category.builder().name( Category.CategoryName.FASHION ).build() );
        categoryMap.put( Category.CategoryName.BOOKS.name(), Category.builder().name( Category.CategoryName.BOOKS ).build() );
        categoryMap.put( Category.CategoryName.MUSIC.name(), Category.builder().name( Category.CategoryName.MUSIC ).build() );
        categoryMap.put( Category.CategoryName.GROCERY.name(), Category.builder().name( Category.CategoryName.GROCERY ).build() );



        // Insertamos categorías
        Flux.just( categoryMap  ).flatMap( map -> {
            return categoryDao.saveAll( map.values() );
        }).doOnNext(category -> {
                categoryMap.put(category.getName().name(), category);
                log.info("Categoría creada: " + category.getName());
        }).thenMany(// Ejecuamos otro flujo de tipo Flux despues (then es para Mono)
            // Insertamos productos
            Flux.just(
                    Product.builder().withName("TV Panasonic Pantalla LCD").withPrice(456.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build(),
                    Product.builder().withName("Sony Camara HD Digital").withPrice(177.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build(),
                    Product.builder().withName("Apple iPod").withPrice(46.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build(),
                    Product.builder().withName("Sony Notebook").withPrice(846.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build(),
                    Product.builder().withName("Hewlett Packard Multifuncional").withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).withPrice(200.89).build(),
                    Product.builder().withName("Bianchi Bicicleta").withPrice(70.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build(),
                    Product.builder().withName("HP Notebook Omen 17").withPrice(2500.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build(),
                    Product.builder().withName("Mica Cómoda 5 Cajones").withPrice(150.89).withCategory( categoryMap.get( Category.CategoryName.ELECTRONICS.name() ) ).build()
            ).flatMap(productDao::save)
        ) // FlatMap covierte el Mono<Product> a Product
        .subscribe(product -> {
                log.info("Producto creado: " + product.getName() + ", Precio: " + product.getPrice() + " Categoria: " + product.getCategory().getName() );

            });
    }
}
