package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Producto> listarProductos() {
        log.info("Solicitud para listar todos los productos");
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener producto con ID: {}", id);
        Producto producto = productoService.findById(id);
        if (producto == null) {
            log.warn("Producto con ID: {} no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public Producto guardarProducto(@RequestBody Producto producto) {
        log.info("Solicitud para guardar nuevo producto: {}", producto.getNombre());
        return productoService.save(producto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        log.info("Solicitud para actualizar producto con ID: {}", id);
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            producto.setId(id);
            Producto actualizado = productoService.save(producto);
            log.info("Producto con ID: {} actualizado exitosamente", id);
            return ResponseEntity.ok(actualizado);
        }
        log.warn("No se pudo actualizar el producto: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        log.info("Solicitud para eliminar producto con ID: {}", id);
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            log.info("Producto con ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("No se pudo eliminar el producto: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }
}
