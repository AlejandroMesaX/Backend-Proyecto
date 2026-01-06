package com.gofast.domicilios.infrastructure.realtime;

import com.gofast.domicilios.infrastructure.realtime.JwtStompAuthChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;

@Configuration
public class WebSocketSecurityConfig
        extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    private final JwtStompAuthChannelInterceptor jwtStompAuthChannelInterceptor;

    public WebSocketSecurityConfig(
            JwtStompAuthChannelInterceptor jwtStompAuthChannelInterceptor) {
        this.jwtStompAuthChannelInterceptor = jwtStompAuthChannelInterceptor;
    }

    @Override
    protected void configureInbound(
            MessageSecurityMetadataSourceRegistry messages) {

        messages
                // 🔓 ciclo de vida WS
                .simpTypeMatchers(
                        SimpMessageType.CONNECT,
                        SimpMessageType.HEARTBEAT,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.UNSUBSCRIBE
                ).permitAll()

                // 🔐 SOLO ADMIN
                .simpSubscribeDestMatchers("/topic/admin/**")
                .hasRole("ADMIN")

                // opcional
                .simpSubscribeDestMatchers("/topic/**")
                .authenticated()

                .simpDestMatchers("/app/**")
                .authenticated()

                .anyMessage()
                .authenticated();
    }

    @Override
    protected void customizeClientInboundChannel(
            ChannelRegistration registration) {

        // Propaga SecurityContext entre threads
        registration.interceptors(
                new SecurityContextChannelInterceptor());

        // Tu interceptor JWT
        registration.interceptors(
                jwtStompAuthChannelInterceptor);
    }

    @Override
    protected boolean sameOriginDisabled() {
        // 🔑 ESTO DESACTIVA CSRF EN STOMP
        return true;
    }
}
