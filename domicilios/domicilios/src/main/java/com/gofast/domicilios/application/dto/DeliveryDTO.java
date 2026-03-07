package com.gofast.domicilios.application.dto;

import java.time.LocalDateTime;

public record DeliveryDTO(
        Long id,
        String nombre,   // ✅ nombre primero
        String email,    // ✅ email segundo
        String estadoDelivery,
        LocalDateTime disponibleDesde
) {}
