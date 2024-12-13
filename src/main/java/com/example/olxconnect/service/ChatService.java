package com.example.olxconnect.service;

import com.example.olxconnect.dto.ChatComponentsDto;
import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Value("${olx.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    private final MessageService messageService;

    public ChatService(RestTemplate restTemplate, MessageService messageService) {
        this.restTemplate = restTemplate;
        this.messageService = messageService;
    }

    public ChatComponentsDto getChatComponents(String accessToken, Long threadId, Long interlocutorId) {
        try {
            // Pobierz listę wiadomości
            List<MessageDto> messages = messageService.getMessages(accessToken, threadId);

            // Pobierz nazwę użytkownika i avatar na podstawie interlocutorId
            String userName = getUsernameByInterlocutorId(accessToken, interlocutorId);

            // Zbuduj obiekt ChatComponentsDto
            ChatComponentsDto chatComponentsDto = new ChatComponentsDto();
            chatComponentsDto.setUserName(userName);
            chatComponentsDto.setAvatar(null); // Avatar można ustawić, jeśli API go zwraca w przyszłości
            chatComponentsDto.setMessages(messages);

            return chatComponentsDto;

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas pobierania komponentów chatu: " + e.getMessage(), e);
        }
    }

    /**
     * Pobiera nazwę użytkownika na podstawie interlocutorId.
     *
     * @param accessToken    Token dostępu do API.
     * @param interlocutorId ID użytkownika, którego nazwa ma zostać pobrana.
     * @return Nazwa użytkownika lub null, jeśli użytkownik nie został znaleziony.
     */
    public String getUsernameByInterlocutorId(String accessToken, Long interlocutorId) {
        String url = baseUrl + "/users/" + interlocutorId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Version", "2");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UserDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getName();
            } else {
                logger.error("Nie udało się pobrać nazwy użytkownika. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            logger.error("Błąd klienta HTTP przy pobieraniu użytkownika: {}", e.getMessage());
            return null;
        } catch (HttpServerErrorException e) {
            logger.error("Błąd serwera HTTP przy pobieraniu użytkownika: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Nieoczekiwany błąd podczas pobierania nazwy użytkownika: {}", e.getMessage());
            return null;
        }
    }



}
