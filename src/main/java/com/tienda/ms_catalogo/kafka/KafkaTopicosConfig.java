package com.tienda.ms_catalogo.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ms-catalogo es DUENO de:
 *  - pedidos360.navegacion: alto volumen, valor temporal -> 3 particiones, 7 dias, cleanup=delete
 *  - pedidos360.catalogo  : estado actual de cada producto -> cleanup=compact (clave productoId, sin vencimiento)
 */
@Configuration
public class KafkaTopicosConfig {

    @Bean
    public NewTopic topicoNavegacion(@Value("${app.kafka.topicos.navegacion.nombre}") String nombre,
                                     @Value("${app.kafka.topicos.navegacion.particiones}") int particiones,
                                     @Value("${app.kafka.replicas}") int replicas,
                                     @Value("${app.kafka.topicos.navegacion.retencion-ms}") long retencion,
                                     @Value("${app.kafka.topicos.navegacion.min-isr}") int minIsr) {
        return Topicos.crear(nombre, particiones, replicas, retencion, "delete", minIsr);
    }

    @Bean
    public NewTopic topicoCatalogo(@Value("${app.kafka.topicos.catalogo.nombre}") String nombre,
                                   @Value("${app.kafka.topicos.catalogo.particiones}") int particiones,
                                   @Value("${app.kafka.replicas}") int replicas) {
        return Topicos.crear(nombre, particiones, replicas, -1L, "compact", 1);
    }
}
