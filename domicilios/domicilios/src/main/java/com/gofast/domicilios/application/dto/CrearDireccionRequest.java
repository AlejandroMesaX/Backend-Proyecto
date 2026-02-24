package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearDireccionRequest(
        @
                NotNull(message = "barrioId es obligatorio")
        Long barrioId,

        @NotBlank(message = "direccionRecogida es obligatoria")
        String direccionRecogida,

        @NotBlank(message = "telefonoContacto es obligatorio")
        String telefonoContacto
) {
}
