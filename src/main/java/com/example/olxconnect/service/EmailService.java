package com.example.olxconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${mailersend.api-key}")
    private String mailersendApiKey;

    private static final String MAILERSEND_API_URL = "https://api.mailersend.com/v1/email";

    /**
     * Wysyła e-mail za pomocą Mailersend API.
     *
     * @param to      Adres odbiorcy
     * @param subject Temat wiadomości
     * @param text    Treść wiadomości
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        // Tworzenie nagłówków HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mailersendApiKey);

        // Tworzenie ciała żądania
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", Map.of("email", "powiadomienia@stanislawnowak.pl", "name", "Stanislaw Nowak"));
        requestBody.put("to", new Object[]{Map.of("email", to, "name", to)});
        requestBody.put("subject", subject);
        requestBody.put("text", text);

        // Konfiguracja żądania
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Wysyłanie żądania HTTP POST
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    MAILERSEND_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("E-mail wysłany do: " + to);
            } else {
                System.err.println("Błąd podczas wysyłania e-maila. Status: " + response.getStatusCode());
                System.err.println("Treść odpowiedzi: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas wysyłania e-maila: " + e.getMessage());
            throw new RuntimeException("Nie udało się wysłać e-maila.", e);
        }
    }
}
