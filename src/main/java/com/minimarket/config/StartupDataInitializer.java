package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class StartupDataInitializer {

    @Bean
    public CommandLineRunner seedUsers(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Rol gerente = findOrCreateRole(rolRepository, "GERENTE");
            Rol empleado = findOrCreateRole(rolRepository, "EMPLEADO");
            Rol cliente = findOrCreateRole(rolRepository, "CLIENTE");

            findOrCreateUser(usuarioRepository, passwordEncoder, "gerente", "gerente123", gerente);
            findOrCreateUser(usuarioRepository, passwordEncoder, "empleado", "empleado123", empleado);
            findOrCreateUser(usuarioRepository, passwordEncoder, "cliente", "cliente123", cliente);
        };
    }

    private Rol findOrCreateRole(RolRepository rolRepository, String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(nombre);
                    return rolRepository.save(rol);
                });
    }

    private void findOrCreateUser(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            Rol role
    ) {
        usuarioRepository.findByUsername(username).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuario.setRoles(Set.of(role));
            usuario.setAccountNonExpired(true);
            usuario.setAccountNonLocked(true);
            usuario.setCredentialsNonExpired(true);
            usuario.setEnabled(true);
            return usuarioRepository.save(usuario);
        });
    }
}