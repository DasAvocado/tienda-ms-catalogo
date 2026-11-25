package com.tienda.ms_catalogo.config;

import com.tienda.ms_catalogo.messaging.CatalogoPublisher;
import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Catalogo de ejemplo con CATEGORIAS (las usa ms-recomendaciones).
 * Inserta los productos que falten (por nombre), completa la categoria de los antiguos
 * y al terminar publica el catalogo en Kafka (topico compactado pedidos360.catalogo + PRODUCTO_CATALOGO).
 */
@Configuration
public class DatosIniciales {

    private static final List<Producto> EJEMPLO = List.of(
            new Producto(null, "Filtro de Aceite", "Filtro de aceite para motor", 15000.0, 50, "Motor"),
            new Producto(null, "Aceite Sintetico 5W-30 4L", "Aceite de motor sintetico, bidon de 4 litros", 32990.0, 40, "Motor"),
            new Producto(null, "Filtro de Aire", "Filtro de aire de alto flujo", 12990.0, 35, "Motor"),
            new Producto(null, "Bujias Iridium x4", "Juego de 4 bujias de iridio", 28990.0, 25, "Motor"),
            new Producto(null, "Pastillas de Freno", "Juego de pastillas delanteras", 45000.0, 30, "Frenos"),
            new Producto(null, "Discos de Freno (par)", "Par de discos ventilados delanteros", 69990.0, 15, "Frenos"),
            new Producto(null, "Liquido de Frenos DOT4", "Liquido de frenos DOT4 500 ml", 7990.0, 60, "Frenos"),
            new Producto(null, "Bateria 12V", "Bateria 12V 60Ah", 85000.0, 20, "Electrico"),
            new Producto(null, "Ampolletas LED H4 (par)", "Par de ampolletas LED H4 6000K", 19990.0, 30, "Electrico"),
            new Producto(null, "Cables Puente 3 m", "Cables para partir con bateria, 3 metros", 14990.0, 25, "Electrico"));

    @Bean
    CommandLineRunner cargarProductos(ProductoRepository repo, CatalogoPublisher catalogoPublisher) {
        return args -> {
            Map<String, String> categoriaPorNombre = EJEMPLO.stream()
                    .collect(Collectors.toMap(p -> p.getNombre().toLowerCase(), Producto::getCategoria));
            List<Producto> existentes = repo.findAll();
            Set<String> nombres = existentes.stream().map(p -> p.getNombre().toLowerCase()).collect(Collectors.toSet());

            for (Producto p : existentes) {
                if (p.getCategoria() == null) {
                    p.setCategoria(categoriaPorNombre.getOrDefault(p.getNombre().toLowerCase(), "General"));
                    repo.save(p);
                }
            }
            for (Producto p : EJEMPLO) {
                if (!nombres.contains(p.getNombre().toLowerCase())) {
                    repo.save(new Producto(null, p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock(), p.getCategoria()));
                }
            }
            // En segundo plano: si Kafka aun no esta listo, el arranque del servicio no espera.
            List<Producto> catalogo = repo.findAll();
            CompletableFuture.runAsync(() -> catalogo.forEach(catalogoPublisher::publicar));
        };
    }
}
