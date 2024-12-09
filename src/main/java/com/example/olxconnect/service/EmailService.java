package com.example.olxconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final String MAILERSEND_API_URL = "https://api.mailersend.com/v1/email";

    @Value("${mailersend.api.token}")
    private String mailerSendApiToken;

    public void sendEmail(String from, String to, String subject, String text) {
        RestTemplate restTemplate = new RestTemplate();

        // Nagłówki z tokenem API
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + mailerSendApiToken); // Pobranie tokenu z Config Vars
        headers.set("Content-Type", "application/json");

        // Treść zapytania
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", Map.of("email", from, "name", "OLX Connector")); // Nadawca
        requestBody.put("to", new Object[] { Map.of("email", to, "name", "User") }); // Odbiorca
        requestBody.put("subject", subject);
        requestBody.put("text", text);

        // Tworzenie żądania
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Wysłanie zapytania POST
            ResponseEntity<String> response = restTemplate.exchange(
                    MAILERSEND_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("E-mail wysłany pomyślnie do: " + to);
            } else {
                System.err.println("Błąd podczas wysyłania e-maila: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Błąd: " + e.getMessage());
        }
    }
}
