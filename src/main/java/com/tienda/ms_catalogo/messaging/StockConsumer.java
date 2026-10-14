package com.tienda.ms_catalogo.messaging;

import com.tienda.ms_catalogo.config.RabbitConfig;
import com.tienda.ms_catalogo.dto.OrdenCreadaEvent;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Descuenta stock cuando ms-ordenes confirma una orden. */
@Component
public class StockConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockConsumer.class);

    private final ProductoRepository productos;

    public StockConsumer(ProductoRepository productos) {
        this.productos = productos;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_DESCONTAR_STOCK)
    @Transactional
    public void onOrdenCreada(OrdenCreadaEvent evento) {
        log.info("[RabbitMQ] orden.creada recibida: ordenId={}", evento.ordenId());

        for (OrdenCreadaEvent.Item item : evento.items()) {
            productos.findById(item.productoId()).ifPresentOrElse(producto -> {
                int stockActual = producto.getStock() == null ? 0 : producto.getStock();
                if (stockActual >= item.cantidad()) {
                    producto.setStock(stockActual - item.cantidad());
                    productos.save(producto);
                    log.info("Stock actualizado: producto {} ({}) {} -> {}",
                            producto.getId(), producto.getNombre(), stockActual, producto.getStock());
                } else {
                    log.warn("Stock insuficiente para producto {} (stock={}, pedido={}) en orden {}",
                            producto.getId(), stockActual, item.cantidad(), evento.ordenId());
                }
            }, () -> log.warn("Producto {} no existe (orden {})", item.productoId(), evento.ordenId()));
        }
    }
}
