package com.minimarket.security.config;

import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de Spring Security.
 *
 * - Deshabilita el formulario de login por defecto
 * - Configura la aplicación como STATELESS (sin sesiones)
 * - Define qué rutas son públicas y cuáles requieren autenticación/rol
 * - Registra el filtro JWT antes del filtro estándar de Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Habilita @PreAuthorize en los controla
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    
        /**
     * Proveedor de autenticación: une el UserDetailsService con el PasswordEncoder.
     * Spring Security usará esto para verificar credenciales en el login.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Configuración de encriptación de contraseñas
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                // Desabilitar CSRF (Cross-Site Request Forgery) ya que usaremos JWT y no sesiones
                .csrf(csrf -> csrf.disable())


                // Configurar la gestión de sesiones como STATELESS (sin estado)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


                // Reglas de autorizacion
                .authorizeHttpRequests(auth -> auth

                    // Rutas públicas
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/login/**").permitAll()

                    // Rutas que requieren rol GERENTE
                    .requestMatchers("/api/admin/**").hasRole("GERENTE")

                    // Rutas que requieren rol GERENTE o EMPLEADO
                    .requestMatchers("/api/inventario/**").hasAnyRole("GERENTE", "EMPLEADO")
                    .requestMatchers("/api/ventas/**").hasAnyRole("GERENTE", "EMPLEADO")
                    .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyRole("GERENTE", "EMPLEADO")
                    .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("GERENTE", "EMPLEADO")
                    .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("GERENTE")
 
                    // Rutas que requieren cualquier usuario autenticado
                    .requestMatchers("/api/carrito/**").hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")

                    // Cualquier otra ruta requiere autenticación
                    .anyRequest().authenticated() 
                )

                // Registrar el filtro JWT antes del filtro de autenticación de Spring Security
                .authenticationProvider(authenticationProvider())

                // Insertar el filtro JWT antes del filtro de usuario y contraseña de Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
