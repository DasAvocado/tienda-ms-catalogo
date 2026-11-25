package com.tienda.ms_catalogo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Infraestructura de PUBLICACION en Kafka (los servicios de negocio no conocen KafkaTemplate).
 * La clave del mensaje (usuarioId) garantiza que todos los eventos de un usuario caen en la misma
 * particion y se procesan EN ORDEN. Kafka aqui es analitica: si el cluster no responde se registra
 * el error y la operacion de negocio (compra, carrito...) NO se cae.
 */
@Component
public class EventoKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventoKafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventoKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(String topico, String clave, Object evento) {
        try {
            kafkaTemplate.send(topico, clave, evento).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error("[Kafka] NO se pudo publicar en {} (clave={}): {}", topico, clave, ex.getMessage());
                } else {
                    log.info("[Kafka] {} -> {}-{}@{} clave={}", evento == null ? "tombstone" : evento.getClass().getSimpleName(), topico,
                            res.getRecordMetadata().partition(), res.getRecordMetadata().offset(), clave);
                }
            });
        } catch (RuntimeException e) {
            log.error("[Kafka] Error enviando a {}: {}", topico, e.getMessage());
        }
    }
}
