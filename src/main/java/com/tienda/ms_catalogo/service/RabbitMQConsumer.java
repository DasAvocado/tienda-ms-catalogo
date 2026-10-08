package com.tienda.ms_catalogo.service;

import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    @Autowired
    private ProductoRepository productoRepository;

    @RabbitListener(queues = "descontar.stock")
    public void descontarStock(String mensaje) {
        // Formato esperado de mensaje simple: "productoId:cantidad" (ej. "1:2")
        try {
            String[] partes = mensaje.split(":");
            Long productoId = Long.parseLong(partes[0].trim());
            Integer cantidad = Integer.parseInt(partes[1].trim());

            productoRepository.findById(productoId).ifPresent(producto -> {
                if (producto.getStock() >= cantidad) {
                    producto.setStock(producto.getStock() - cantidad);
                    productoRepository.save(producto);
                    System.out.println("Stock actualizado para producto " + productoId + ". Nuevo stock: " + producto.getStock());
                } else {
                    System.out.println("Stock insuficiente para el producto " + productoId);
                }
            });
        } catch (Exception e) {
            System.err.println("Error procesando mensaje de stock: " + e.getMessage());
        }
    }
}