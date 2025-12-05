package com.gofast.domicilios.infrastructure.repository;

import com.gofast.domicilios.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UsuarioRepositoryAdapterTest {
    @Autowired
    private UsuarioRepositoryAdapter usuarioRepositoryAdapter;

    @Test
    void guardarYObtenerUsuario(){
        Usuario usuario = new Usuario(
                null,
                "prueba@test.com",
                "123456",
                "User"
        );
    }
}
