package com.codearp.springboot.reactor.springbootsebfluxapirest.configs;

import com.codearp.springboot.reactor.springbootsebfluxapirest.handlers.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class EndpointFunctionalConfig {
// Configucaion basica
//    @Autowired
//    private ProductService productService;
//    @Bean
//    public RouterFunction<ServerResponse> routes() {
//        return RouterFunctions.route(RequestPredicates.GET("/api/v2/")
//                        .or(RequestPredicates.GET("/api/v2/products")),
//                request -> ServerResponse
//                        .ok().contentType(MediaType.APPLICATION_JSON).body(productService.recoverALlProduct(), ProductDto.class)
//        );
//    }

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/v2/")
                        .or(RequestPredicates.GET("/api/v2/products")),
                request -> productHandler.listPeople(request) );
    }
}
