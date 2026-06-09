package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public List<DetalleVenta> listarDetalleVentas() {
        log.info("Solicitud para listar todos los detalles de venta");
        return detalleVentaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<DetalleVenta> obtenerDetalleVentaPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener detalle de venta con ID: {}", id);
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) {
            log.warn("Detalle de venta con ID: {} no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detalleVenta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public DetalleVenta guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        log.info("Solicitud para guardar nuevo detalle de venta");
        return detalleVentaService.save(detalleVenta);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<DetalleVenta> actualizarDetalleVenta(@PathVariable Long id, @RequestBody DetalleVenta detalleVenta) {
        log.info("Solicitud para actualizar detalle de venta con ID: {}", id);
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente != null) {
            detalleVenta.setId(id);
            DetalleVenta actualizado = detalleVentaService.save(detalleVenta);
            log.info("Detalle de venta con ID: {} actualizado exitosamente", id);
            return ResponseEntity.ok(actualizado);
        }
        log.warn("No se pudo actualizar el detalle de venta: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        log.info("Solicitud para eliminar detalle de venta con ID: {}", id);
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            log.info("Detalle de venta con ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("No se pudo eliminar el detalle de venta: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }
}
