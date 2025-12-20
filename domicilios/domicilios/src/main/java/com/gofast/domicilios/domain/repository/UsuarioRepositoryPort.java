package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findAll();

    List<Usuario> findByNombreContains(String nombre);

    List<Usuario> findByRol(String rol);

    void deleteById(Long id);

}
