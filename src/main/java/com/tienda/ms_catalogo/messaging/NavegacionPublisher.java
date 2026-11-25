package com.tienda.ms_catalogo.messaging;

import com.tienda.ms_catalogo.dto.EventoNavegacion;
import com.tienda.ms_catalogo.kafka.EventoKafkaPublisher;
import com.tienda.ms_catalogo.model.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** Productor Kafka del dominio CATALOGO: interacciones del usuario con los productos. */
@Component
public class NavegacionPublisher {

    public static final String PRODUCTO_VISTO = "PRODUCTO_VISTO";
    public static final String PRODUCTO_CATALOGO = "PRODUCTO_CATALOGO";

    private final EventoKafkaPublisher kafka;
    private final String topico;

    public NavegacionPublisher(EventoKafkaPublisher kafka, @Value("${app.kafka.topicos.navegacion.nombre}") String topico) {
        this.kafka = kafka;
        this.topico = topico;
    }

    /** El usuario abrio el DETALLE de un producto (clave = usuarioId -> orden por usuario). */
    public void productoVisto(String usuarioId, Producto p) {
        kafka.publicar(topico, usuarioId, evento(PRODUCTO_VISTO, usuarioId, p, null));
    }

    /** Alta/actualizacion del catalogo: permite al recomendador conocer productos aun no visitados. */
    public void productoCatalogo(Producto p) {
        kafka.publicar(topico, "catalogo", evento(PRODUCTO_CATALOGO, "catalogo", p, "stock=" + p.getStock()));
    }

    private static EventoNavegacion evento(String tipo, String usuarioId, Producto p, String detalle) {
        return new EventoNavegacion(UUID.randomUUID().toString(), tipo, usuarioId, p.getId(), p.getNombre(),
                p.getCategoria(), p.getPrecio(), null, detalle, Instant.now().toString());
    }
}
