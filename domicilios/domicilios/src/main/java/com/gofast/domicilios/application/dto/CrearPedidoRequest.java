package com.gofast.domicilios.application.dto;

import java.math.BigDecimal;

public class CrearPedidoRequest {
    public Long direccionId;

    public String direccionRecogida;
    public String barrioRecogida;
    public String telefonoContactoRecogida;

    public String direccionEntrega;
    public String barrioEntrega;
    public String nombreQuienRecibe;
    public String telefonoQuienRecibe;
}
