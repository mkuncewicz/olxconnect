package com.example.olxconnect.service;

import com.example.olxconnect.dto.MessageDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class MessageService {

    private final RestTemplate restTemplate;

    public MessageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MessageDto> getMessages(String token, Long threadId) {
        String url = "https://api.olx.pl/threads/" + threadId + "/messages";

        // Tworzenie nagłówków z tokenem
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Version", "2");
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<MessageDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MessageDto[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return Arrays.asList(response.getBody());
            } else {
                throw new RuntimeException("Błąd podczas pobierania wiadomości: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas komunikacji z OLX API: " + e.getMessage(), e);
        }
    }
}

