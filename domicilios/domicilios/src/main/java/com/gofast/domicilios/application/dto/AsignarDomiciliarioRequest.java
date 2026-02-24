package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarDomiciliarioRequest(
        @NotNull(message = "domiciliarioId es obligatorio")
        Long domiciliarioId
) {
}
