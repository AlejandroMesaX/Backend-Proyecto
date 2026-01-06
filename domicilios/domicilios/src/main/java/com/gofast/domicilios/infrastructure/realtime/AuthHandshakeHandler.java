package com.gofast.domicilios.infrastructure.realtime;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class AuthHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Object auth = attributes.get("AUTH");
        if (auth instanceof Principal p) {
            return p; // Authentication implementa Principal
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
