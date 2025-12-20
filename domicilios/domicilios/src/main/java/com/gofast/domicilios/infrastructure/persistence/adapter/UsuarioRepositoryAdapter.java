package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import com.gofast.domicilios.domain.model.Rol;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {
    private final UsuarioJpaRepository jpa;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = toEntity(usuario);
        UsuarioEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpa.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public List<Usuario> findByNombreContains(String nombre) {
        return jpa.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Usuario> findByRol(String rol) {
        Rol rolEnum;
        try {
            rolEnum = Rol.valueOf(rol.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Rol inválido: " + rol + ". Use ADMIN, CLIENT o DELIVERY.");
        }

        return jpa.findByRol(rolEnum)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private UsuarioEntity toEntity(Usuario u) {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(u.getId());
        e.setNombre(u.getNombre());
        e.setEmail(u.getEmail());
        e.setPasswordHash(u.getPasswordHash());
        e.setRol(u.getRol());
        e.setActivo(u.isActivo());
        return e;
    }

    private Usuario toDomain(UsuarioEntity e) {
        Usuario u = new Usuario();
        u.setId(e.getId());
        u.setNombre(e.getNombre());
        u.setEmail(e.getEmail());
        u.setPasswordHash(e.getPasswordHash());
        u.setRol(e.getRol());
        u.setActivo(e.isActivo());
        return u;
    }
}
