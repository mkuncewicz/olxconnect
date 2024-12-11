package com.example.olxconnect.service;

import com.example.olxconnect.dto.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final RestTemplate restTemplate;

    public MessageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MessageDto> getMessages(String token, Long threadId) {
        String url = "https://www.olx.pl/api/partner/threads/" + threadId + "/messages";

        // Tworzenie nagłówków z tokenem i wersją API
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Version", "2"); // Dodanie wersji API
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<MessageDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MessageDto[].class
            );

            // Logowanie całej odpowiedzi
            logger.info("Odpowiedź HTTP: {}", response);

            if (response.getStatusCode().is2xxSuccessful()) {
                MessageDto[] body = response.getBody();
                if (body == null || body.length == 0) {
                    throw new RuntimeException("Odpowiedź API nie zawiera wiadomości.");
                }
                return Arrays.asList(body);
            } else {
                throw new RuntimeException("Błąd podczas pobierania wiadomości: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            logger.error("Błąd HTTP w getMessages: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Błąd HTTP podczas pobierania wiadomości: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Ogólny błąd w getMessages: ", e);
            throw new RuntimeException("Nie udało się pobrać wiadomości z OLX API.", e);
        }
    }

}

