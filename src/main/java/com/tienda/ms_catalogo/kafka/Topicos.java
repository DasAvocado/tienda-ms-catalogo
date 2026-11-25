package com.tienda.ms_catalogo.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.kafka.config.TopicBuilder;

/** Fabrica de NewTopic con los parametros de administracion definidos en application.yml (app.kafka.topicos.*). */
public final class Topicos {

    private Topicos() {}

    public static NewTopic crear(String nombre, int particiones, int replicas, long retencionMs,
                                 String cleanupPolicy, int minIsr) {
        return TopicBuilder.name(nombre)
                .partitions(particiones)
                .replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retencionMs))
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, cleanupPolicy)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(Math.min(minIsr, replicas)))
                .build();
    }
}
