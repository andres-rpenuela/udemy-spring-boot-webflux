package com.codearp.springboot.reactor.springbootsebfluxapirest.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        // Configura el ObjectMapper según tus necesidades
        // Por ejemplo, puedes registrar módulos adicionales o configurar opciones de serialización
        return objectMapper;
    }
}
