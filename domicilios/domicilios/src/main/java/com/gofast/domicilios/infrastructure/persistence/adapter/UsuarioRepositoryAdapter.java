package com.gofast.domicilios.infrastructure.persistence.adapter;

import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.domain.repository.UsuarioRepositoryPort;
import com.gofast.domicilios.infrastructure.persistence.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.data.jpa.domain.Specification;
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

//    @Override
//    public void deleteById(Long id) {
//        jpa.deleteById(id);
//    }


    @Override
    public List<Usuario> findByFiltros(String nombre, Rol rol, Boolean activo) {

        Specification<UsuarioEntity> spec =
                (root, query, cb) -> cb.conjunction();

        if (nombre != null && !nombre.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("nombre")),
                            "%" + nombre.toLowerCase() + "%"
                    )
            );
        }

        if (rol != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("rol"), rol)
            );
        }

        if (activo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("activo"), activo)
            );
        }

        return jpa.findAll(spec)
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
        e.setDisponibleDesde(u.getDisponibleDesde());
        e.setEstadoDelivery(u.getEstadoDelivery());
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
        u.setEstadoDelivery(e.getEstadoDelivery());
        u.setDisponibleDesde(e.getDisponibleDesde());
        return u;
    }
}
