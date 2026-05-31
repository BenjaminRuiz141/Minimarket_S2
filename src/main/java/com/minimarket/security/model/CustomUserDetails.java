package com.minimarket.security.model;

import com.minimarket.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return usuario.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return usuario.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return usuario.isEnabled();
    }
}
