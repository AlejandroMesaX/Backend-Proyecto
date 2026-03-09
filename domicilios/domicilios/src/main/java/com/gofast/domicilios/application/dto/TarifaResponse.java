package com.gofast.domicilios.application.dto;

import java.math.BigDecimal;

public record TarifaResponse(
        String barrioRecogida,
        String barrioEntrega,
        BigDecimal costo
) {}
