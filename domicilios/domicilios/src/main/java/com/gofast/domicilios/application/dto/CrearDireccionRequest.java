package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearDireccionRequest(
        @NotBlank(message = "Barrio es obligatorio")
        String barrio,

        @NotBlank(message = "direccionRecogida es obligatoria")
        String direccionRecogida,

        @NotBlank(message = "telefonoContacto es obligatorio")
        String telefonoContacto
) {
}
