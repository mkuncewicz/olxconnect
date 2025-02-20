package com.example.olxconnect.service;

import com.example.olxconnect.dto.ApiResponseWrapper;
import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.dto.MessageResponse;
import com.example.olxconnect.dto.ThreadResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private TokenService tokenService;

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

    public List<MessageDto> getMessagesByRefreshToken(String refreshToken, Long threadId) {

        String stringTokenByRefreshToken = tokenService.getStringTokenByRefreshToken(refreshToken);

        return getMessages(stringTokenByRefreshToken,threadId);
    }


    public ThreadResponseDto getThreadFromApi(String token, Long threadId) {
        String url = "https://www.olx.pl/api/partner/threads/" + threadId;

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
                // Odczyt JSON-a z opakowaniem
                ApiResponseWrapper<ThreadResponseDto> wrapper = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<ApiResponseWrapper<ThreadResponseDto>>() {}
                );

                ThreadResponseDto threadResponse = wrapper.getData();

                if (threadResponse == null || threadResponse.getId() == null || threadResponse.getTotalCount() == null) {
                    throw new IllegalArgumentException("Niepełne dane w odpowiedzi z API OLX.");
                }

                return threadResponse;
            } else {
                logger.error("Błąd podczas pobierania wątku. Status: {}", response.getStatusCode());
                throw new RuntimeException("Błąd podczas pobierania wątku: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Błąd HTTP w getThreadFromApi: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Błąd HTTP podczas pobierania wątku: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Ogólny błąd w getThreadFromApi: ", e);
            throw new RuntimeException("Nie udało się pobrać wątku z OLX API.", e);
        }
    }



    public void sendMessage(String token, Long threadId, String text, List<String> attachmentUrls) {
        String url = "https://www.olx.pl/api/partner/threads/" + threadId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Version", "2");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Budowanie ciała wiadomości
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);

        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            List<Map<String, String>> attachments = new ArrayList<>();
            for (String attachmentUrl : attachmentUrls) {
                Map<String, String> attachment = new HashMap<>();
                attachment.put("url", attachmentUrl);
                attachments.add(attachment);
            }
            requestBody.put("attachments", attachments);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Wiadomość została pomyślnie wysłana.");
            } else {
                logger.error("Błąd podczas wysyłania wiadomości. Status: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Błąd HTTP podczas wysyłania wiadomości: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Błąd HTTP podczas wysyłania wiadomości.", e);
        } catch (Exception e) {
            logger.error("Nieoczekiwany błąd podczas wysyłania wiadomości: ", e);
            throw new RuntimeException("Nie udało się wysłać wiadomości.", e);
        }
    }

    public void markThreadAsRead(String token, Long threadId) {
        String url = "https://www.olx.pl/api/partner/threads/" + threadId + "/commands"; //

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("command", "mark-as-read");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            logger.info("Wątek ID " + threadId + " oznaczony jako przeczytany.");
        } else {
            logger.warn("Nie udało się oznaczyć wątku ID " + threadId + " jako przeczytanego.");
        }
    }

}
