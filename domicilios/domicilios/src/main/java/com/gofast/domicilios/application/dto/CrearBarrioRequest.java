package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearBarrioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotNull(message = "La comuna es obligatoria")
        Integer comunaNumero) {
}
