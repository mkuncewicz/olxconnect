package com.example.olxconnect.service;

import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {


    private static final Logger logger = LoggerFactory.getLogger(OlxService.class);

    @Value("${olx.client_id}")
    private String clientId;

    @Value("${olx.client_secret}")
    private String clientSecret;

    @Value("${olx.redirect_uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://www.olx.pl/api/open/oauth/token";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RestTemplate restTemplate;


    public Token getTokenByRefreshToken(String refreshToken) {

        Token tokenDB = tokenRepository.findByRefreshToken(refreshToken);

        return tokenDB;
    }

    public String getStringTokenByRefreshToken(String refrestToken){

        String accessToken = tokenRepository.findAccessTokenByRefreshToken(refrestToken);

        return accessToken;
    }

    @Scheduled(fixedRate = 1800000) // Co 30 minut
    public void refreshTokens() {
        List<Token> tokens = tokenRepository.findAll(); // Pobierz wszystkie tokeny z bazy danych

        for (Token token : tokens) {
            if (shouldRefreshToken(token)) { // Sprawdź, czy token wymaga odświeżenia
                try {
                    Token refreshedToken = refreshToken(token); // Odśwież token
                    if (refreshedToken != null) {
                        tokenRepository.save(refreshedToken); // Zapisz zaktualizowany token w bazie danych
                        logger.info("Token refreshed for user: {}", token.getUsername());
                    }
                } catch (Exception e) {
                    logger.error("Failed to refresh token for user: {}", token.getUsername(), e);
                }
            }
        }
    }

    public void updateUserName(Long tokenId, String username) {

        Optional<Token> optionalToken = tokenRepository.findById(tokenId);
        if (optionalToken.isPresent()) {
            Token token = optionalToken.get();
            token.setUsername(username);
            tokenRepository.save(token);
        }else {
            logger.info("TokenId nie istnieje");
        }
    }

    /**
     * Sprawdza, czy token wymaga odświeżenia (pozostało mniej niż 1 godzina).
     */
    private boolean shouldRefreshToken(Token token) {
        return token.getExpiration().isBefore(LocalDateTime.now().plusHours(1)); // Jeśli zostało mniej niż 1 godzina
    }

    private boolean shouldGenerateNewRefreshToken(Token token) {
        return token.getExpiration().isBefore(LocalDateTime.now().plusDays(3));
    }

    private Token refreshToken(Token token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Budowanie ciała żądania
        String body = "grant_type=refresh_token"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + token.getRefreshToken();

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                String newAccessToken = (String) responseBody.get("access_token");
                String newRefreshToken = (String) responseBody.get("refresh_token");
                int expiresIn = (int) responseBody.get("expires_in");

                // Aktualizacja tokenu
                token.setAccessToken(newAccessToken);
                token.setRefreshToken(newRefreshToken);
                token.setExpiration(LocalDateTime.now().plusSeconds(expiresIn));

                return token; // Zwróć zaktualizowany token
            } else {
                logger.error("Failed to refresh token. Response: {}", response.getBody());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while refreshing token for user: {}", token.getUsername(), e);
            return null;
        }
    }
}
