package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CrearComunaRequest(
        @NotNull(message = "El número de comuna es obligatorio")
        @Positive(message = "El número de comuna debe ser mayor a 0")
        Integer numero,

        @NotNull(message = "La tarifa base es obligatoria")
        @DecimalMin(value = "0.0", message = "La tarifa base debe ser mayor o igual a 0")
        BigDecimal tarifaBase,

        @NotNull(message = "El recargo por salto es obligatorio")
        @DecimalMin(value = "0.0", message = "El recargo por salto debe ser mayor o igual a 0")
        BigDecimal recargoPorSalto
) {
}
