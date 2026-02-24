package com.gofast.domicilios.application.dto;

public record EditarDireccionRequest(
        Long barrioId,
        String direccionRecogida,
        String telefonoContacto,
        Boolean activo
) {
}
