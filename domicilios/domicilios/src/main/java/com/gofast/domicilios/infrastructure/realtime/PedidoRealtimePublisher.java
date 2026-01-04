package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.application.dto.PedidoDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoRealtimePublisher {
    private final SimpMessagingTemplate template;

    public PedidoRealtimePublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void pedidoCreado(PedidoDTO dto) {
        template.convertAndSend("/topic/admin/pedidos", dto);
    }
}
