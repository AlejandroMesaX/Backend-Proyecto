package com.gofast.domicilios.application.dto;

import java.math.BigDecimal;

public class PedidoDTO {
    public Long id;
    public Long clienteId;
    public Long domiciliarioId;
    public String estado;
    public BigDecimal costoServicio;
    public String fechaCreacion;

    public String direccionRecogida;
    public String barrioRecogida;
    public String telefonoContactoRecogida;

    public String direccionEntrega;
    public String barrioEntrega;
    public String nombreQuienRecibe;
    public String telefonoQuienRecibe;
}
