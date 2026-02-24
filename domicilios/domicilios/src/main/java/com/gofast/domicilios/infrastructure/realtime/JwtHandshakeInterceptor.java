package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // Leer Authorization del handshake HTTP (Upgrade)
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Alternativa: token por query param ?token= (por si el browser no manda headers en WS)
        if ((authHeader == null || authHeader.isBlank()) && request instanceof ServletServerHttpRequest sreq) {
            HttpServletRequest servletReq = sreq.getServletRequest();
            String tokenParam = servletReq.getParameter("token");
            if (tokenParam != null && !tokenParam.isBlank()) {
                authHeader = "Bearer " + tokenParam;
            }
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token, userDetailsService);

                attributes.put("AUTH", auth);

                auth.getAuthorities().forEach(a -> System.out.println("WS HANDSHAKE AUTH=" + a.getAuthority()));
                return true;
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) { }
}
