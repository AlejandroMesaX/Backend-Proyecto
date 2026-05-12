package com.gofast.domicilios.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final String apiKey;
    private final String fromEmail;
    private final RestTemplate restTemplate;

    public EmailService(
            @Value("${resend.api.key:}") String apiKey,
            @Value("${resend.from.email:onboarding@resend.dev}") String fromEmail) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.restTemplate = new RestTemplate();
    }

    public void enviarCodigoVerificacion(String destinatario, String nombre, String codigo) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("API key de Resend no configurada");
        }

        String html = buildHtml(nombre, codigo);

        Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", List.of(destinatario),
                "subject", "GoFast — Tu código de verificación",
                "html", html
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.resend.com/emails",
                    request,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.ACCEPTED) {
                throw new RuntimeException("Error de Resend: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo: " + e.getMessage());
        }
    }

    private String buildHtml(String nombre, String codigo) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#1c1c1e;font-family:sans-serif;">
              <div style="max-width:480px;margin:40px auto;background:#2a2a2d;border-radius:16px;
                          border:1px solid #3f3f44;overflow:hidden;">
                <div style="background:#f5c518;padding:24px;text-align:center;">
                  <h1 style="margin:0;color:#0d0d0d;font-size:24px;font-weight:800;">🚀 GoFast</h1>
                </div>
                <div style="padding:32px 28px;">
                  <p style="color:#f0f0f0;font-size:16px;margin:0 0 8px;">
                    Hola, <strong>%s</strong> 👋
                  </p>
                  <p style="color:#9ca3af;font-size:14px;margin:0 0 28px;">
                    Usa este código para verificar tu cuenta. Expira en <strong style="color:#f5c518;">15 minutos</strong>.
                  </p>
                  <div style="text-align:center;margin:0 0 28px;">
                    <div style="display:inline-block;background:#333336;border:2px solid #f5c518;
                                border-radius:12px;padding:16px 32px;">
                      <span style="color:#f5c518;font-size:36px;font-weight:800;letter-spacing:10px;">
                        %s
                      </span>
                    </div>
                  </div>
                  <p style="color:#6b7280;font-size:12px;margin:0;text-align:center;">
                    Si no solicitaste este código, ignora este correo.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombre, codigo);
    }
}