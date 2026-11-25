package com.tienda.ms_catalogo.controller;

import com.tienda.ms_catalogo.messaging.CatalogoPublisher;
import com.tienda.ms_catalogo.messaging.NavegacionPublisher;
import com.tienda.ms_catalogo.model.Producto;
import com.tienda.ms_catalogo.repository.ProductoRepository;
import com.tienda.ms_catalogo.security.Seguridad;
import com.tienda.ms_catalogo.security.UsuarioActual;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints (via API Gateway se mantiene el mismo prefijo):
 *   GET    /api/catalogo        -> lista de productos           (scope + rol CLIENTE/ADMIN)
 *   GET    /api/catalogo/{id}   -> detalle + evento Kafka PRODUCTO_VISTO (scope + rol CLIENTE/ADMIN)
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
    private final NavegacionPublisher navegacion;
    private final CatalogoPublisher catalogo;

    public CatalogoController(ProductoRepository productos, NavegacionPublisher navegacion, CatalogoPublisher catalogo) {
        this.productos = productos;
        this.navegacion = navegacion;
        this.catalogo = catalogo;
    }

    @GetMapping
    @PreAuthorize(Seguridad.CLIENTE)
    public List<Producto> listar() {
        return productos.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Seguridad.CLIENTE)
    public ResponseEntity<Producto> obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return productos.findById(id).map(p -> {
            navegacion.productoVisto(UsuarioActual.id(jwt), p);
            return ResponseEntity.ok(p);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize(Seguridad.ADMIN)
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        producto.setId(null);
        Producto creado = productos.save(producto);
        catalogo.publicar(creado);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Seguridad.ADMIN)
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto datos) {
        return productos.findById(id).map(p -> {
            p.setNombre(datos.getNombre());
            p.setDescripcion(datos.getDescripcion());
            p.setPrecio(datos.getPrecio());
            p.setStock(datos.getStock());
            if (datos.getCategoria() != null) p.setCategoria(datos.getCategoria());
            Producto guardado = productos.save(p);
            catalogo.publicar(guardado);
            return ResponseEntity.ok(guardado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Seguridad.ADMIN)
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!productos.existsById(id)) return ResponseEntity.notFound().build();
        productos.deleteById(id);
        catalogo.eliminado(id);
        return ResponseEntity.noContent().build();
    }
}
