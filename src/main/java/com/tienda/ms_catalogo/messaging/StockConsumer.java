package com.tienda.ms_catalogo.messaging;

import com.rabbitmq.client.Channel;
import com.tienda.ms_catalogo.dto.OrdenCreadaEvent;
import com.tienda.ms_catalogo.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio CATALOGO/STOCK - consumidor de "orden.creada".
 * Solo recibe y delega: el descuento de stock esta en StockService y el ACK en ConfirmacionMensajes.
 */
@Component
public class StockConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockConsumer.class);

    private final StockService stockService;
    private final ConfirmacionMensajes confirmacion;

    public StockConsumer(StockService stockService, ConfirmacionMensajes confirmacion) {
        this.stockService = stockService;
        this.confirmacion = confirmacion;
    }

    @RabbitListener(queues = "${app.rabbit.queues.descontar-stock}", ackMode = "MANUAL")
    public void onOrdenCreada(@Payload OrdenCreadaEvent evento, Message mensaje, Channel canal) throws IOException {
        log.info("[RabbitMQ] orden.creada recibida: ordenId={}", evento.ordenId());
        confirmacion.procesar(mensaje, canal, () -> {
            if (evento.ordenId() == null || evento.items() == null || evento.items().isEmpty()) {
                throw new MensajeInvalidoException("orden.creada sin ordenId o sin items");
            }
            stockService.descontar(evento);
        });
    }
}
