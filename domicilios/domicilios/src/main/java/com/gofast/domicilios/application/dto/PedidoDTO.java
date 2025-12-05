package com.gofast.domicilios.application.dto;

import java.math.BigDecimal;

public class PedidoDTO {
    public Long id;
    public Long clienteId;
    public Long direccionId;
    public Long domiciliarioId;
    public String estado;
    public BigDecimal total;
    public String fechaCreacion;
}
