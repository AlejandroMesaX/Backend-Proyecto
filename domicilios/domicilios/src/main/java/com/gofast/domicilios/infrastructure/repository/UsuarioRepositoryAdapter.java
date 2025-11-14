package com.gofast.domicilios.infrastructure.repository;

import com.gofast.domicilios.domain.model.Rol;
import com.gofast.domicilios.domain.repository.UsuarioRepository;
import com.gofast.domicilios.domain.model.Usuario;
import com.gofast.domicilios.infrastructure.entity.UsuarioEntity;
import com.gofast.domicilios.infrastructure.entity.RolEntity;
import  org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final JpaRolRepository jpaRolRepository;

    public UsuarioRepositoryAdapter(JpaUsuarioRepository jpaUsuarioRepository,
                                    JpaRolRepository jpaRolRepository){
        this.jpaUsuarioRepository = jpaUsuarioRepository;
        this.jpaRolRepository = jpaRolRepository;
    }

    @Override
    public Usuario save(Usuario usuario){
        RolEntity rolEntity = jpaRolRepository.findByNombre(usuario.getRol().getNombre()).
                orElseThrow(() -> new RuntimeException("Rol No Encontrado" + usuario.getRol().getNombre()));

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNombre(usuario.getNombre());
        entity.setEmail(usuario.getEmail());
        entity.setPassword(usuario.getPassword());
        entity.setRol(rolEntity);
        UsuarioEntity saved = jpaUsuarioRepository.save(entity);

        Rol rol = new Rol(saved.getRol().getId(), saved.getRol().getNombre());
        return new Usuario(
                saved.getId(),
                saved.getNombre(),
                saved.getEmail(),
                saved.getPassword(),
                rol);
    }

    @Override
    public Optional<Usuario> findById(Long id){
        return jpaUsuarioRepository.findById(id)
                .map(e -> new Usuario(
                                                   e.getId(),
                                                   e.getNombre(),
                                                   e.getEmail(),
                                                   e.getPassword(),
                        new Rol(e.getRol().getId(), e.getRol().getNombre())));
    }

    @Override
    public List<Usuario> findAll(){
        return jpaUsuarioRepository.findAll()
                .stream()
                .map(e -> new Usuario(
                                                   e.getId(),
                                                   e.getNombre(),
                                                   e.getEmail(),
                                                   e.getPassword(),
                        new Rol(e.getRol().getId(), e.getRol().getNombre())))
                .toList();
    }

    @Override
    public void deleteById(Long id){
        jpaUsuarioRepository                         .deleteById(id);
    }
}
