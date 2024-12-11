package com.example.olxconnect.service;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.dto.MessageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MessageService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<MessageDto> getMessages(String token, Long threadId) {
        String url = "https://www.olx.pl/api/partner/threads/" + threadId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Version", "2");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                MessageResponse messageResponse = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<MessageResponse>() {}
                );

                return messageResponse.getData();
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
