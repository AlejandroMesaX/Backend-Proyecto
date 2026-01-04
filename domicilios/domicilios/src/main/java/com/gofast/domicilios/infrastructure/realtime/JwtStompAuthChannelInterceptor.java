package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.infrastructure.security.CustomUserDetailsService;
import com.gofast.domicilios.infrastructure.security.JwtTokenProvider;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtStompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtStompAuthChannelInterceptor(JwtTokenProvider jwtTokenProvider,
                                          CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Falta Authorization: Bearer <token>");
            }

            String token = authHeader.substring(7);

            // ✅ aquí usamos TU provider
            if (!jwtTokenProvider.validateToken(token)) {
                throw new MessagingException("JWT inválido");
            }

            // ✅ Extraer username/email del token
            // Ajusta el nombre del método al que ya tengas en tu provider:
            String username = jwtTokenProvider.getUsernameFromToken(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            accessor.setUser(authentication);
        }

        return message;
    }
}
