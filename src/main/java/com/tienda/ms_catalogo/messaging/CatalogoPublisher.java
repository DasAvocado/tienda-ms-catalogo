package com.tienda.ms_catalogo.messaging;

import com.tienda.ms_catalogo.dto.ProductoCatalogoEvento;
import com.tienda.ms_catalogo.kafka.EventoKafkaPublisher;
import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * Publica el ESTADO de cada producto (precio y stock) en el topico compactado "pedidos360.catalogo":
 * Kafka conserva siempre el ultimo mensaje de cada productoId, asi ms-carrito arma su tabla de precios
 * oficiales leyendo el topico desde el inicio (event-carried state transfer, sin llamadas HTTP entre servicios).
 * Tambien avisa al recomendador (PRODUCTO_CATALOGO en el topico de navegacion).
 */
@Component
public class CatalogoPublisher {

    private final EventoKafkaPublisher kafka;
    private final NavegacionPublisher navegacion;
    private final ProductoRepository productos;
    private final String topico;

    public CatalogoPublisher(EventoKafkaPublisher kafka, NavegacionPublisher navegacion, ProductoRepository productos,
                             @Value("${app.kafka.topicos.catalogo.nombre}") String topico) {
        this.kafka = kafka;
        this.navegacion = navegacion;
        this.productos = productos;
        this.topico = topico;
    }

    public void publicar(Producto p) {
        kafka.publicar(topico, String.valueOf(p.getId()), new ProductoCatalogoEvento(UUID.randomUUID().toString(),
                p.getId(), p.getNombre(), p.getCategoria(), p.getPrecio(), p.getStock(), Instant.now().toString()));
        navegacion.productoCatalogo(p);
    }

    /** Tras descontar stock: publica el nuevo stock de los productos de la orden. */
    public void publicarPorId(Collection<Long> ids) {
        productos.findAllById(ids).forEach(this::publicar);
    }

    /** Producto eliminado: mensaje "tombstone" (valor null) -> la compactacion borra la clave del topico. */
    public void eliminado(Long id) {
        kafka.publicar(topico, String.valueOf(id), null);
    }
}
