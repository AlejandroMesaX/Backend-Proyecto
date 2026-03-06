package com.gofast.domicilios.application.dto;

import java.time.LocalDateTime;

public record DeliveryDTO(
        Long id,
        String email,
        String nombre,
        String estadoDelivery,
        LocalDateTime disponibleDesde
) {

}
