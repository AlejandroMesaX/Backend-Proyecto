package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.infrastructure.security.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtStompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // ✅ Auth cache por sesión STOMP
    private final Map<String, Authentication> sessionAuth = new ConcurrentHashMap<>();

    public JwtStompAuthChannelInterceptor(JwtTokenProvider jwtTokenProvider,
                                          UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();

        // ✅ Para cualquier frame posterior, reinyecta el auth guardado
        if (sessionId != null && accessor.getUser() == null) {
            Authentication saved = sessionAuth.get(sessionId);
            if (saved != null) {
                accessor.setUser(saved);
                SecurityContextHolder.getContext().setAuthentication(saved);
            }
        }

        // ✅ En CONNECT, crea el auth desde JWT y guárdalo
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication =
                            jwtTokenProvider.getAuthentication(token, userDetailsService);

                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (sessionId != null) {
                        sessionAuth.put(sessionId, authentication);
                    }

                    System.out.println("WS CONNECT session=" + sessionId + " user=" + authentication.getName());
                    authentication.getAuthorities()
                            .forEach(a -> System.out.println("WS CONNECT AUTH=" + a.getAuthority()));
                }
            }
        }

        // ✅ Limpieza
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (sessionId != null) sessionAuth.remove(sessionId);
        }

        // ✅ DEBUG CLAVE: mira quién llega en SUBSCRIBE
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            System.out.println("WS SUBSCRIBE session=" + accessor.getSessionId()
                    + " dest=" + accessor.getDestination()
                    + " user=" + (accessor.getUser() != null ? accessor.getUser().getName() : "NULL"));
        }

        return message;
    }
}
