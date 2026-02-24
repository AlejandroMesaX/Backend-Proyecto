package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportarIncidenciaRequest(
        @NotBlank(message = "motivo es obligatorio")
        String motivo
) {
}
