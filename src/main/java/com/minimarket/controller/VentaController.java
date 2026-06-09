package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public List<Venta> listarVentas() {
        log.info("Solicitud para listar todas las ventas");
        return ventaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener venta con ID: {}", id);
        Venta venta = ventaService.findById(id);
        if (venta == null) {
            log.warn("Venta con ID: {} no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(venta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public Venta guardarVenta(@RequestBody Venta venta) {
        log.info("Solicitud para registrar nueva venta");
        return ventaService.save(venta);
    }
}
