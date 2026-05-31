package com.minimarket.security.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
 
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    /**
     * Clave secreta en Base64 (mínimo 256 bits para HS256).
     * En application.properties: jwt.secret=TuClaveSecretaMuyLargaEnBase64AquiMinimo256Bits
     *
     * Genera una en terminal con:
     *   openssl rand -base64 64
     */
    @Value("${jwt.secret}")
    private String secretKey;
 
        /**
     * Tiempo de expiración del token en milisegundos.
     * En application.properties: jwt.expiration=86400000  (24 horas)
     */
    @Value("${jwt.expiration}")
    private long expirationMs;

        /**
     * Genera un JWT firmado para el usuario autenticado.
     * El token incluye como claims: username (sub), roles, y fecha de emisión/expiración.
     *
     * @param userDetails objeto UserDetails cargado por CustomUserDetailsService
     * @return String con el token JWT completo (header.payload.signature)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
 
        // Incluir los roles en el payload del token
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
 
        return buildToken(claims, userDetails.getUsername());
    }
 
    /**
     * Construye y firma el token JWT usando HMAC-SHA256 (HS256).
     */
    private String buildToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)                          // "sub": username del usuario
                .issuedAt(new Date())                       // "iat": fecha de emisión
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp"
                .signWith(getSigningKey())                  // firma con clave HMAC-SHA256
                .compact();
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // VALIDACIÓN
    // ─────────────────────────────────────────────────────────────────────────
 
    /**
     * Valida el token: verifica firma, expiración y que el subject coincida con el usuario.
     *
     * @param token       el JWT recibido en el header Authorization
     * @param userDetails el usuario cargado desde la base de datos
     * @return true si el token es válido para ese usuario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
 
    /**
     * Verifica si el token ya expiró.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // EXTRACCIÓN DE DATOS
    // ─────────────────────────────────────────────────────────────────────────
 
    /**
     * Extrae el username (campo "sub") del token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
 
    /**
     * Extrae la lista de roles del token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) extractAllClaims(token).get("roles");
    }
 
    /**
     * Extrae la fecha de expiración del token.
     */
    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
 
    /**
     * Parsea y retorna todos los claims del token.
     * Lanza excepción si la firma es inválida o el token está malformado/expirado.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
 
    /**
     * Decodifica la clave secreta desde Base64 y crea un objeto SecretKey seguro.
     * JJWT rechazará claves menores a 256 bits para HS256 (requisito del estándar RFC 7518).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
