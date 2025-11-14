package com.gofast.domicilios.domain.repository;

import com.gofast.domicilios.domain.model.Usuario;
import java.util.Optional;
import java.util.List;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
    void deleteById(Long id);
}
