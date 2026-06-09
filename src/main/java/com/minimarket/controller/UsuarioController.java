package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('GERENTE')")
    public List<Usuario> listarUsuarios() {
        log.info("Solicitud para listar todos los usuarios");
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener usuario con ID: {}", id);
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isEmpty()) {
            log.warn("Usuario con ID: {} no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario.get());
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public Usuario guardarUsuario(@RequestBody Usuario usuario) {
        log.info("Solicitud para guardar nuevo usuario: {}", usuario.getUsername());
        return usuarioService.save(usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        log.info("Solicitud para actualizar usuario con ID: {}", id);
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuario.setId(id);
            Usuario actualizado = usuarioService.save(usuario);
            log.info("Usuario con ID: {} actualizado exitosamente", id);
            return ResponseEntity.ok(actualizado);
        }
        log.warn("No se pudo actualizar el usuario: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        log.info("Solicitud para eliminar usuario con ID: {}", id);
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            log.info("Usuario con ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("No se pudo eliminar el usuario: ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }
}
