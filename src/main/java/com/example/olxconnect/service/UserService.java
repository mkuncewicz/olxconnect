package com.example.olxconnect.service;

import com.example.olxconnect.dto.UserDto;
import com.example.olxconnect.dto.UserResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    private final RestTemplate restTemplate;

    @Value("${olx.api.base-url}")
    private String baseUrl;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Pobiera dane użytkownika na podstawie ID.
     *
     * @param token Token dostępu do API.
     * @param userId      ID użytkownika, którego dane mają zostać pobrane.
     * @return Obiekt UserDto lub null, jeśli użytkownik nie został znaleziony.
     */
    public UserDto getUserById(String token, Long userId) {
        String url = "https://www.olx.pl/api/partner/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Version", "2");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UserResponseDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getData(); // Pobieramy obiekt użytkownika z pola `data`
            } else {
                logger.error("Nie udało się pobrać danych użytkownika. Status: {}, Body: {}",
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
            logger.error("Nieoczekiwany błąd podczas pobierania użytkownika: {}", e.getMessage());
            return null;
        }
    }

}
