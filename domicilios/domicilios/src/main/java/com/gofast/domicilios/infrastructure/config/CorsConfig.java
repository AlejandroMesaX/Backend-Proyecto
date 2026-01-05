package com.gofast.domicilios.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ tu frontend
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // ✅ métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // ✅ headers permitidos (incluye Authorization para JWT)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // ✅ expone headers si los necesitas
        config.setExposedHeaders(List.of("Authorization"));

        // Si usas cookies en el futuro: true (y no puedes usar "*")
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}