package com.gofast.domicilios.application.dto;

public record EditarUsuarioRequest(
        String nombre,
        String rol,
        Boolean activo
) {
}
