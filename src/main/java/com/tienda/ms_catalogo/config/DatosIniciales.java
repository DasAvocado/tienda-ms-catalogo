package com.tienda.ms_catalogo.config;

import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Carga productos de ejemplo la primera vez (tabla vacia). */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarProductos(ProductoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Producto(null, "Filtro de Aceite", "Filtro de aceite para motor", 15000.0, 50));
                repo.save(new Producto(null, "Pastillas de Freno", "Juego de pastillas delanteras", 45000.0, 30));
                repo.save(new Producto(null, "Bateria 12V", "Bateria 12V 60Ah", 85000.0, 20));
            }
        };
    }
}
