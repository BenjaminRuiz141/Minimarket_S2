package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public List<Carrito> listarCarrito() {
        log.info("Solicitud para listar carritos");
        return carritoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<Carrito> obtenerCarritoPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener carrito con ID: {}", id);
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) {
            log.warn("Carrito con ID: {} no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carrito);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public Carrito agregarProductoAlCarrito(@RequestBody Carrito carrito) {
        log.info("Solicitud para agregar producto al carrito");
        return carritoService.save(carrito);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<Carrito> actualizarCarrito(@PathVariable Long id, @RequestBody Carrito carrito) {
        log.info("Solicitud para actualizar carrito con ID: {}", id);
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            carrito.setId(id);
            Carrito actualizado = carritoService.save(carrito);
            log.info("Carrito con ID: {} actualizado exitosamente", id);
            return ResponseEntity.ok(actualizado);
        }
        log.warn("No se pudo actualizar el carrito: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        log.info("Solicitud para eliminar carrito con ID: {}", id);
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            log.info("Carrito con ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("No se pudo eliminar el carrito: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }
}
