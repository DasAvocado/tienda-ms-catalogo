package com.tienda.ms_catalogo.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente Kafka (productor) de este microservicio.
 *  - bootstrap: los 3 brokers del cluster (app.kafka.bootstrap-servers)
 *  - key = String (usuarioId) / value = DTO serializado a JSON (EventoJsonSerializer)
 *  - acks=all + idempotencia: el mensaje se confirma cuando lo tienen todas las replicas en sincronia (min.insync)
 *    y los reintentos no generan duplicados.
 *  - KafkaAdmin crea al arrancar los NewTopic declarados (ver KafkaTopicosConfig) si no existen.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${app.kafka.bootstrap-servers}")
    private String bootstrap;

    @Value("${spring.application.name}")
    private String aplicacion;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        cfg.put(ProducerConfig.CLIENT_ID_CONFIG, aplicacion + "-producer");
        cfg.put(ProducerConfig.ACKS_CONFIG, "all");
        cfg.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        cfg.put(ProducerConfig.RETRIES_CONFIG, 5);
        cfg.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        cfg.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        cfg.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        cfg.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        cfg.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);   // si Kafka esta caido, no bloquear la peticion HTTP
        return new DefaultKafkaProducerFactory<>(cfg, new StringSerializer(), new EventoJsonSerializer());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        KafkaAdmin admin = new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap));
        admin.setFatalIfBrokerNotAvailable(false);   // el servicio arranca aunque Kafka aun no este listo
        admin.setModifyTopicConfigs(true);            // aplica retention/cleanup del yml si el topico ya existia
        return admin;
    }
}
