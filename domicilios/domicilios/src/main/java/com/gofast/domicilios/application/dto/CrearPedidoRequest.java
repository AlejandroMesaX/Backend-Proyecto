package com.gofast.domicilios.application.dto;

import jakarta.validation.constraints.NotBlank;

public class CrearPedidoRequest {
    public Long direccionId;

    public String direccionRecogida;
    public String barrioRecogida;
    public String telefonoContactoRecogida;

    @NotBlank(message = "direccionEntrega es obligatoria")
    public String direccionEntrega;

    @NotBlank(message = "barrioEntrega es obligatorio")
    public String barrioEntrega;

    @NotBlank(message = "nombreQuienRecibe es obligatorio")
    public String nombreQuienRecibe;

    @NotBlank(message = "telefonoQuienRecibe es obligatorio")
    public String telefonoQuienRecibe;
}
