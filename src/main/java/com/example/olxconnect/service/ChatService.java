package com.example.olxconnect.service;

import com.example.olxconnect.dto.ChatComponentsDto;
import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

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

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserDto> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    requestEntity,
                    UserDto.class
            );

            return response.getBody() != null ? response.getBody().getName() : null;

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas pobierania nazwy użytkownika: " + e.getMessage(), e);
        }
    }


}
