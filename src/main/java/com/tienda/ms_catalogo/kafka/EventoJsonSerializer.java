package com.tienda.ms_catalogo.kafka;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Serializador Kafka: convierte cualquier DTO (record) a JSON UTF-8 con Jackson 3.
 * Si el valor ya es byte[] (p.ej. un mensaje defectuoso que se reenvia al topico .dlt) lo deja tal cual.
 */
public class EventoJsonSerializer implements Serializer<Object> {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public byte[] serialize(String topic, Object data) {
        if (data == null) return null;
        if (data instanceof byte[] bytes) return bytes;
        try {
            return mapper.writeValueAsBytes(data);
        } catch (RuntimeException e) {
            throw new SerializationException("No se pudo serializar " + data.getClass().getSimpleName()
                    + " para el topico " + topic, e);
        }
    }
}
