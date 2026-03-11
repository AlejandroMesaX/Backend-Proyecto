package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.application.dto.BarrioDTO;
import com.gofast.domicilios.application.dto.DeliveryDTO;
import com.gofast.domicilios.application.dto.PedidoDTO;
import com.gofast.domicilios.application.dto.UsuarioDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RealtimePublisher {

    private final SimpMessagingTemplate messaging;

    public RealtimePublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    // Admin topics
    public void pedidoActualizado(PedidoDTO dto) {

        messaging.convertAndSend("/topic/admin/pedidos", dto);
    }

    public void deliveryActualizado(DeliveryDTO dto) {

        messaging.convertAndSend("/topic/admin/domiciliarios", dto);
    }

    // Delivery topic por id (no requiere email en DTO)
    public void pedidoParaDelivery(Long deliveryId, PedidoDTO dto) {
        messaging.convertAndSend("/topic/delivery/" + deliveryId + "/pedidos", dto);
    }

    public void pedidoParaCliente(Long clienteId, PedidoDTO dto) {
        messaging.convertAndSend("/topic/cliente/" + clienteId + "/pedidos", dto);
    }

    // Barrios: cuando se activa/desactiva un barrio
    public void barrioActualizado(BarrioDTO dto) {
        messaging.convertAndSend("/topic/barrios", dto);
    }

    public void usuarioActualizado(UsuarioDTO dto) {
        messaging.convertAndSend("/topic/admin/usuarios", dto);
    }
}
