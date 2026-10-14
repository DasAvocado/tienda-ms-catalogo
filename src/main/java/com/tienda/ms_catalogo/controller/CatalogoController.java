package com.tienda.ms_catalogo.controller;

import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import com.tienda.ms_catalogo.security.Seguridad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints (via API Gateway se mantiene el mismo prefijo):
 *   GET    /api/catalogo        -> lista de productos           (scope + rol CLIENTE/ADMIN)
 *   GET    /api/catalogo/{id}   -> detalle                      (scope + rol CLIENTE/ADMIN)
 *   POST   /api/catalogo        -> crear producto               (rol ADMIN)
 *   PUT    /api/catalogo/{id}   -> actualizar producto          (rol ADMIN)
 *   DELETE /api/catalogo/{id}   -> eliminar producto            (rol ADMIN)
 *
 * CORS lo resuelve el API Gateway; no se declara aqui para evitar cabeceras duplicadas.
 */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final ProductoRepository productos;

    public CatalogoController(ProductoRepository productos) {
        this.productos = productos;
    }

    @GetMapping
    @PreAuthorize(Seguridad.CLIENTE)
    public List<Producto> listar() {
        return productos.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Seguridad.CLIENTE)
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return productos.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize(Seguridad.ADMIN)
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        producto.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(productos.save(producto));
    }

    @PutMapping("/{id}")
    @PreAuthorize(Seguridad.ADMIN)
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto datos) {
        return productos.findById(id).map(p -> {
            p.setNombre(datos.getNombre());
            p.setDescripcion(datos.getDescripcion());
            p.setPrecio(datos.getPrecio());
            p.setStock(datos.getStock());
            return ResponseEntity.ok(productos.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Seguridad.ADMIN)
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!productos.existsById(id)) return ResponseEntity.notFound().build();
        productos.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
