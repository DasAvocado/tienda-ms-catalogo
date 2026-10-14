package com.tienda.ms_catalogo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ de ms-catalogo. Los NOMBRES se definen solo en application.yml (app.rabbit.*).
 *
 *   [pedidos360.events] --orden.creada--> catalogo.descontar-stock   (consume StockConsumer)
 *   catalogo.descontar-stock --(rechazo/reintentos agotados)--> [pedidos360.dlx] --> catalogo.descontar-stock.dlq
 *
 * Mapa global de eventos (exchange topic "pedidos360.events"):
 *   carrito.checkout      -> publicado por ms-carrito, consumido por ms-ordenes
 *   orden.creada          -> publicado por ms-ordenes, consumido por ms-catalogo (stock),
 *                            ms-notificaciones (correo) y ms-auditoria
 *   notificacion.enviada  -> publicado por ms-notificaciones, consumido por ms-auditoria
 */
@Configuration
public class RabbitConfig {

    private final String exchange;
    private final String dlx;
    private final String rkOrdenCreada;
    private final String colaStock;
    private final String colaStockDlq;

    public RabbitConfig(@Value("${app.rabbit.exchange}") String exchange,
                        @Value("${app.rabbit.dlx}") String dlx,
                        @Value("${app.rabbit.routing-keys.orden-creada}") String rkOrdenCreada,
                        @Value("${app.rabbit.queues.descontar-stock}") String colaStock,
                        @Value("${app.rabbit.queues.descontar-stock-dlq}") String colaStockDlq) {
        this.exchange = exchange;
        this.dlx = dlx;
        this.rkOrdenCreada = rkOrdenCreada;
        this.colaStock = colaStock;
        this.colaStockDlq = colaStockDlq;
    }

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlx, true, false);
    }

    // ---------- Caso de uso: descontar stock tras una compra ----------
    @Bean
    public Queue colaDescontarStock() {
        return QueueBuilder.durable(colaStock)
                .deadLetterExchange(dlx)
                .deadLetterRoutingKey(colaStockDlq)
                .build();
    }

    @Bean
    public Binding bindingDescontarStock() {
        return BindingBuilder.bind(colaDescontarStock()).to(eventosExchange()).with(rkOrdenCreada);
    }

    @Bean
    public Queue colaDescontarStockDlq() {
        return QueueBuilder.durable(colaStockDlq).build();
    }

    @Bean
    public Binding bindingDescontarStockDlq() {
        return BindingBuilder.bind(colaDescontarStockDlq()).to(deadLetterExchange()).with(colaStockDlq);
    }

    /** Spring Boot lo asocia automaticamente al RabbitTemplate y a los @RabbitListener. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
