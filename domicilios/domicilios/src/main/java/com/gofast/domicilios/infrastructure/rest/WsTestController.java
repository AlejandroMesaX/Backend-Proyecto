package com.gofast.domicilios.infrastructure.rest;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ws-test")
public class WsTestController {

    private final SimpMessagingTemplate template;

    public WsTestController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @PostMapping("/ping")
    public void ping() {
        template.convertAndSend("/topic/admin/pedidos", "PING_OK");
    }
}
