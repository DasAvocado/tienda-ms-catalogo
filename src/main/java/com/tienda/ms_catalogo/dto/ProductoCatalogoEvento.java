package com.tienda.ms_catalogo.dto;

/** DTO del topico COMPACTADO "pedidos360.catalogo" (clave = productoId): ultimo estado de cada producto. */
public record ProductoCatalogoEvento(String eventoId, Long productoId, String nombre, String categoria,
                                     Double precio, Integer stock, String fecha) {}
