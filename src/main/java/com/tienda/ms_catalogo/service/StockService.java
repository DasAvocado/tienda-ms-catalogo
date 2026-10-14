package com.tienda.ms_catalogo.service;

import com.tienda.ms_catalogo.dto.OrdenCreadaEvent;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logica de negocio del inventario (sin detalles de RabbitMQ). */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final ProductoRepository productos;

    public StockService(ProductoRepository productos) {
        this.productos = productos;
    }

    /** Descuenta el stock de cada item. Si algo falla, la transaccion completa se revierte (y el mensaje se reintenta). */
    @Transactional
    public void descontar(OrdenCreadaEvent evento) {
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
