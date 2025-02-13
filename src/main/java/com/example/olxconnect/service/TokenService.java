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

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${olx.client_id}")
    private String clientId;

    @Value("${olx.client_secret}")
    private String clientSecret;

    @Value("${notification.recipient-email}")
    private String recipientEmail;

    private static final String TOKEN_URL = "https://www.olx.pl/api/open/oauth/token";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmailService emailService; // Dodajemy emailService do wysyłania powiadomień

    /**
     * Pobiera token na podstawie refresh tokena.
     */
    public Token getTokenByRefreshToken(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken);
    }

    /**
     * Pobiera access token na podstawie refresh tokena.
     */
    public String getStringTokenByRefreshToken(String refreshToken) {
        return tokenRepository.findAccessTokenByRefreshToken(refreshToken);
    }

    /**
     * Harmonogram odświeżania tokenów (co 30 minut).
     */
    @Scheduled(fixedRate = 1800000) // Co 30 minut
    public void refreshTokens() {
        List<Token> tokens = tokenRepository.findAll();

        for (Token token : tokens) {
            if (shouldRefreshToken(token)) {
                try {
                    Token refreshedToken = refreshToken(token);

                    if (refreshedToken != null) {
                        refreshedToken.setMessageIsSent(false); // Resetujemy flagę po udanym odświeżeniu
                        tokenRepository.save(refreshedToken);
                        logger.info("Token refreshed for user: {}", token.getUsername());
                    } else {
                        handleExpiredRefreshToken(token);
                    }
                } catch (Exception e) {
                    logger.error("Błąd podczas odświeżania tokena dla użytkownika: {}", token.getUsername(), e);
                }
            }
        }
    }

    /**
     * Aktualizuje nazwę użytkownika przypisaną do tokena.
     */
    public void updateUserName(Long tokenId, String username) {
        Optional<Token> optionalToken = tokenRepository.findById(tokenId);
        if (optionalToken.isPresent()) {
            Token token = optionalToken.get();
            token.setUsername(username);
            tokenRepository.save(token);
        } else {
            logger.info("TokenId {} nie istnieje", tokenId);
        }
    }

    /**
     * Sprawdza, czy token wymaga odświeżenia (pozostało mniej niż 1 godzina).
     */
    private boolean shouldRefreshToken(Token token) {
        return token.getExpiration().isBefore(LocalDateTime.now().plusHours(1));
    }

    /**
     * Obsługa przypadku wygasłego refresh tokena.
     */
    private void handleExpiredRefreshToken(Token token) {
        if (!token.isMessageIsSent()) {
            notifyExpiredRefreshToken(token);
            token.setMessageIsSent(true);
            tokenRepository.save(token);
        } else {
            logger.info("Powiadomienie o wygasłym tokenie dla {} już zostało wysłane.", token.getUsername());
        }
    }

    /**
     * Wysyła powiadomienie e-mail o wygasłym refresh tokenie.
     */
    private void notifyExpiredRefreshToken(Token token) {
        String emailContent = String.format(
                "Twój refresh token dla konta %s wygasł i nie może zostać odświeżony. Zaloguj się ponownie, aby odnowić dostęp.",
                token.getUsername()
        );

        try {
            emailService.sendEmail(
                    "test@stanislawnowak.pl",
                    recipientEmail,
                    "Twój refresh token wygasł",
                    emailContent
            );

            logger.info("Powiadomienie e-mail wysłane do: {}", token.getUsername());
        } catch (Exception e) {
            logger.error("Błąd podczas wysyłania powiadomienia o wygasłym refresh tokenie: {}", e.getMessage());
        }
    }

    /**
     * Wysyła żądanie do API OLX w celu odświeżenia tokena.
     */
    private Token refreshToken(Token token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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

                token.setAccessToken(newAccessToken);
                token.setRefreshToken(newRefreshToken);
                token.setExpiration(LocalDateTime.now().plusSeconds(expiresIn));

                return token;
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
