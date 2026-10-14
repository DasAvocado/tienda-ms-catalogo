package com.tienda.ms_catalogo.service;

import com.tienda.ms_catalogo.dto.OrdenCreadaEvent;
import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockServiceTest {

    private static OrdenCreadaEvent orden(int cantidad) {
        return new OrdenCreadaEvent(1L, "u1", "a@b.cl", "Ana", 15000.0 * cantidad, "2026-01-01T00:00:00Z",
                List.of(new OrdenCreadaEvent.Item(5L, "Filtro", 15000, cantidad)));
    }

    @Test
    void descuentaStock() {
        ProductoRepository repo = mock(ProductoRepository.class);
        Producto p = new Producto(5L, "Filtro", "", 15000.0, 10);
        when(repo.findById(5L)).thenReturn(Optional.of(p));

        new StockService(repo).descontar(orden(3));

        assertEquals(7, p.getStock());
        verify(repo).save(p);
    }

    @Test
    void noDescuentaSiNoHayStock() {
        ProductoRepository repo = mock(ProductoRepository.class);
        Producto p = new Producto(5L, "Filtro", "", 15000.0, 1);
        when(repo.findById(5L)).thenReturn(Optional.of(p));

        new StockService(repo).descontar(orden(3));

        assertEquals(1, p.getStock());
        verify(repo, never()).save(any());
    }
}
