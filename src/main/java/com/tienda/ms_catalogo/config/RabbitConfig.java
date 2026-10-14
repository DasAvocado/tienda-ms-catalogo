package com.tienda.ms_catalogo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ de Pedidos360 (la misma exchange la declaran todos los servicios):
 *
 *   exchange topic "pedidos360.events"
 *     carrito.checkout      -> publicado por ms-carrito, consumido por ms-ordenes
 *     orden.creada          -> publicado por ms-ordenes, consumido por ms-catalogo (stock),
 *                              ms-notificaciones (correo) y ms-auditoria
 *     notificacion.enviada  -> publicado por ms-notificaciones
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos360.events";
    public static final String RK_ORDEN_CREADA = "orden.creada";
    public static final String QUEUE_DESCONTAR_STOCK = "catalogo.descontar-stock";

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaDescontarStock() {
        return QueueBuilder.durable(QUEUE_DESCONTAR_STOCK).build();
    }

    @Bean
    public Binding bindingDescontarStock() {
        return BindingBuilder.bind(colaDescontarStock()).to(eventosExchange()).with(RK_ORDEN_CREADA);
    }

    /** Spring Boot lo asocia automaticamente al RabbitTemplate y a los @RabbitListener. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
