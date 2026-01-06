package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.application.dto.PedidoDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public PedidoRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void pedidoCreado(PedidoDTO dto) {
        System.out.println("📢 WS SEND /topic/admin/pedidos id=");
        messagingTemplate.convertAndSend("/topic/admin/pedidos", dto);
    }
}
