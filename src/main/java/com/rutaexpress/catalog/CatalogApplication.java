package com.rutaexpress.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CatalogApplication {

    private static final Logger log = LoggerFactory.getLogger(CatalogApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }

    @Bean
    CommandLineRunner startupBanner(@Value("${server.port}") String port) {
        return args -> {
            log.info("🗂️ ms-rutaexpress-catalog arriba en el puerto {}", port);
            log.info("💲 Endpoints: /servicios (CRUD) y /vehiculos (CRUD + disponibilidad)");
            log.info("🔒 Sin validación de JWT propia — confía en que solo el BFF lo llama");
        };
    }
}
