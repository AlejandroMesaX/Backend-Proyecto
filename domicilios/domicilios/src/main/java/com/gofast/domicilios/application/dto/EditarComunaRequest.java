package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record EditarComunaRequest(
        @DecimalMin(value = "0.0", message = "La tarifa base debe ser mayor o igual a 0")
        BigDecimal tarifaBase,

        @DecimalMin(value = "0.0", message = "El recargo por salto debe ser mayor o igual a 0")
        BigDecimal recargoPorSalto
) {
}
