package com.codearp.springboot.reactor.springbootsebfluxapirest;

import com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Category;
import com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Product;
import com.codearp.springboot.reactor.springbootsebfluxapirest.shared.CATEGORY;
import com.codearp.springboot.reactor.springbootsebfluxapirest.dtos.ProductDto;
import com.codearp.springboot.reactor.springbootsebfluxapirest.repositories.CategoryDao;
import com.codearp.springboot.reactor.springbootsebfluxapirest.repositories.ProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class Application implements ApplicationRunner {

    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        productDao.findAll().then( productDao.deleteAll() ).block();
        categoryDao.findAll().then( categoryDao.deleteAll() ).block();


        for(CATEGORY cat : CATEGORY.values()) {
            Category category = new Category();
            category.setName( cat.name() );

            categoryDao.save( category ).subscribe();
        }

        List<ProductDto> products = List.of(
                ProductDto.builder().name("TV Panasonic Pantalla LCD").category(CATEGORY.ELECTRONICS).price(456.89).build(),
                ProductDto.builder().name("Sony Camara HD Digital").category(CATEGORY.ELECTRONICS).price(177.89).build(),
                ProductDto.builder().name("Apple iPod").category(CATEGORY.ELECTRONICS).price(46.89).build(),
                ProductDto.builder().name("Sony Notebook").category(CATEGORY.ELECTRONICS).price(846.89).build(),
                ProductDto.builder().name("Hewlett Packard Multifuncional").category(CATEGORY.ELECTRONICS).price(200.89).build(),
                ProductDto.builder().name("Bianchi Bicicleta").category(CATEGORY.ELECTRONICS).price(70.89).build(),
                ProductDto.builder().name("HP Notebook Omen 17").category(CATEGORY.ELECTRONICS).price(2500.89).build(),
                ProductDto.builder().name("Mica Cómoda 5 Cajones").category(CATEGORY.ELECTRONICS).price(150.89).build(),
                ProductDto.builder().name("TV Sony Bravia OLED 4K Ultra HD").category(CATEGORY.ELECTRONICS).price(2255.89).build()
        );

        for( ProductDto p : products ) {
            categoryDao.findByName( p.getCategory().name() )
                    .flatMap( cat -> {
                        Product pNew = new Product();
                        pNew.setName( p.getName() );
                        pNew.setPrice( p.getPrice() );
                        pNew.setCategory( cat );
                        pNew.setCategory( cat );
                        pNew.setCreateAt( new Date() );

                        return productDao.save( pNew );
                    })
                    .subscribe();

        }
    }
}
