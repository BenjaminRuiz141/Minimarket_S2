package com.minimarket.security.util;

import com.minimarket.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;
 
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 
    @Autowired
    private JwtUtil jwtUtil;
 
    @Autowired
    private CustomUserDetailsService userDetailsService;
 
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
 
        // 1. Leer el header Authorization
        final String authHeader = request.getHeader("Authorization");
 
        // Si no hay header o no empieza con "Bearer ", pasar al siguiente filtro sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
 
        // 2. Extraer el token (quitar el prefijo "Bearer ")
        final String jwt = authHeader.substring(7);
        String username = null;
 
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token malformado, firma inválida o expirado: no autenticar
            filterChain.doFilter(request, response);
            return;
        }
 
        // 3. Solo continuar si se extrajo username Y aún no hay autenticación en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
 
            try {
                // 4. Cargar los datos del usuario desde la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!userDetails.isAccountNonExpired()
                        || !userDetails.isAccountNonLocked()
                        || !userDetails.isCredentialsNonExpired()
                        || !userDetails.isEnabled()) {
                    filterChain.doFilter(request, response);
                    return;
                }
 
                // 5. Validar el token contra ese usuario
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
 
                    // 6. Crear el objeto de autenticación con los roles del usuario
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                           // credentials null (ya autenticado con JWT)
                                    userDetails.getAuthorities()    // roles/permisos del usuario
                            );
 
                    // Adjuntar detalles del request (IP, session ID, etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
 
                    // 7. Registrar la autenticación en el SecurityContext de Spring
                    // A partir de aquí, Spring Security reconoce al usuario como autenticado
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                // Usuario no existe en la BD: no autenticar y dejar que el filtro de seguridad maneje el acceso
                log.warn("Intento de acceso con token de usuario inexistente: {}", username);
            } catch (Exception e) {
                // Cualquier otro error durante la carga del usuario
                log.error("Error inesperado al cargar el usuario {}: {}", username, e.getMessage());
            }
        }
 
        // 8. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
