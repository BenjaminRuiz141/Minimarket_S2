package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Categoria> listarCategorias() {
        log.info("Solicitud para listar todas las categorías");
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener categoría con ID: {}", id);
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) {
            log.warn("Categoría con ID: {} no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categoria);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public Categoria guardarCategoria(@RequestBody Categoria categoria) {
        log.info("Solicitud para guardar nueva categoría: {}", categoria.getNombre());
        return categoriaService.save(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        log.info("Solicitud para actualizar categoría con ID: {}", id);
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            categoria.setId(id);
            Categoria actualizada = categoriaService.save(categoria);
            log.info("Categoría con ID: {} actualizada exitosamente", id);
            return ResponseEntity.ok(actualizada);
        }
        log.warn("No se pudo actualizar la categoría: ID {} no encontrada", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        log.info("Solicitud para eliminar categoría con ID: {}", id);
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            log.info("Categoría con ID: {} eliminada exitosamente", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("No se pudo eliminar la categoría: ID {} no encontrada", id);
        return ResponseEntity.notFound().build();
    }
}
