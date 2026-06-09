package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public List<Inventario> listarMovimientosDeInventario() {
        log.info("Solicitud para listar todos los movimientos de inventario");
        return inventarioService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Inventario> obtenerMovimientoPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener movimiento de inventario con ID: {}", id);
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) {
            log.warn("Movimiento de inventario con ID: {} no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inventario);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public Inventario registrarMovimiento(@RequestBody Inventario inventario) {
        log.info("Solicitud para registrar nuevo movimiento de inventario");
        return inventarioService.save(inventario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Inventario> actualizarMovimiento(@PathVariable Long id, @RequestBody Inventario inventario) {
        log.info("Solicitud para actualizar movimiento de inventario con ID: {}", id);
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            Inventario actualizado = inventarioService.save(inventario);
            log.info("Movimiento de inventario con ID: {} actualizado exitosamente", id);
            return ResponseEntity.ok(actualizado);
        }
        log.warn("No se pudo actualizar el movimiento de inventario: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        log.info("Solicitud para eliminar movimiento de inventario con ID: {}", id);
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            log.info("Movimiento de inventario con ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("No se pudo eliminar el movimiento de inventario: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }
}
