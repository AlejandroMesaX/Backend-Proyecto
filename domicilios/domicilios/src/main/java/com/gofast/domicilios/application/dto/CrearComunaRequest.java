package com.gofast.domicilios.application.dto;

import java.math.BigDecimal;

public class CrearComunaRequest {
    public Integer numero;              // Ej: 1, 2, 3
    public BigDecimal tarifaBase;       // Ej: 6000
    public BigDecimal recargoPorSalto;
}
