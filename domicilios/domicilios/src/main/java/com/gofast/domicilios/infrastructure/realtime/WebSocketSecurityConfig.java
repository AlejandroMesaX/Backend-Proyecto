package com.gofast.domicilios.infrastructure.realtime;

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
                .simpTypeMatchers(
                        SimpMessageType.CONNECT,
                        SimpMessageType.HEARTBEAT,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.UNSUBSCRIBE
                ).permitAll()

                .simpSubscribeDestMatchers("/topic/admin/**")
                .hasRole("ADMIN")

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

        registration.interceptors(
                new SecurityContextChannelInterceptor());

        registration.interceptors(
                jwtStompAuthChannelInterceptor);
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
