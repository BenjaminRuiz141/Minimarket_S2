package com.minimarket.security.service;
 
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
 
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controlador de autenticación — rutas públicas (no requieren JWT).
 *
 * Endpoints:
 *   POST /api/auth/login    → autentica y retorna JWT
 *   POST /api/auth/registro → registra nuevo usuario
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
 
    @Autowired
    private AuthenticationManager authenticationManager;
 
    @Autowired
    private JwtUtil jwtUtil;
 
    @Autowired
    private UsuarioRepository usuarioRepository;
 
    @Autowired
    private RolRepository rolRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder;
 
    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────
 
    /**
     * Autentica al usuario y retorna un JWT.
     *
     * Request body:
     * {
     *   "username": "juan",
     *   "password": "miContraseña123"
     * }
     *
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "tipo": "Bearer",
     *   "username": "juan"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Delegar autenticación a Spring Security (verifica usuario y contraseña BCrypt)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
 
            // 2. Obtener los detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
 
            // 3. Generar el JWT con los datos del usuario (username + roles)
            String jwt = jwtUtil.generateToken(userDetails);
 
            // 4. Retornar el token al cliente
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("tipo", "Bearer");
            response.put("username", userDetails.getUsername());
 
            return ResponseEntity.ok(response);
 
        } catch (BadCredentialsException e) {
            // No revelar si el error es de usuario o contraseña (buena práctica de seguridad)
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRO
    // ─────────────────────────────────────────────────────────────────────────
 
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Request body:
     * {
     *   "username": "juan",
     *   "password": "miContraseña123",
     *   "rol": "CLIENTE"           ← opcional: CLIENTE (default), EMPLEADO, GERENTE
     * }
     *
     * Response (201 Created):
     * {
     *   "mensaje": "Usuario registrado exitosamente",
     *   "username": "juan"
     * }
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, String> registroRequest) {
        String username = registroRequest.get("username");
        String password = registroRequest.get("password");
        String rolNombre = registroRequest.getOrDefault("rol", "CLIENTE");
 
        // Validar que no exista el username
        if (usuarioRepository.findByUsername(username).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "El username ya está en uso");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
 
        // Validar que el rol exista en la base de datos
        Rol rol = rolRepository.findByNombre(rolNombre.toUpperCase())
                .orElseGet(() -> rolRepository.findByNombre("CLIENTE")
                        .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado en la BD")));
 
        // Crear y guardar el nuevo usuario con la contraseña hasheada con BCrypt
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(passwordEncoder.encode(password)); // NUNCA guardar en texto plano
 
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        nuevoUsuario.setRoles(roles);
 
        usuarioRepository.save(nuevoUsuario);
 
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario registrado exitosamente");
        response.put("username", username);
 
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

