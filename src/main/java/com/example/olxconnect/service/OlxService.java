package com.example.olxconnect.service;

import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class OlxService {

    private static final Logger logger = LoggerFactory.getLogger(OlxService.class);

    @Value("${olx.client_id}")
    private String clientId;

    @Value("${olx.client_secret}")
    private String clientSecret;

    @Value("${olx.redirect_uri}")
    private String redirectUri;

    private static final String STATE = "abc123";
    private static final String SCOPE = "read write v2";
    private static final String AUTH_URL = "https://www.olx.pl/oauth/authorize/";
    private static final String TOKEN_URL = "https://www.olx.pl/api/open/oauth/token";

    @Autowired
    private TokenRepository tokenRepository;

    public String getAuthorizationUrl() {
        return String.format(
                "%s?client_id=%s&response_type=code&state=%s&scope=%s&redirect_uri=%s",
                AUTH_URL, clientId, STATE, SCOPE.replace(" ", "+"), redirectUri
        );
    }

    @Async("taskExecutor")
    public CompletableFuture<ResponseEntity<String>> createAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Użycie LinkedMultiValueMap zamiast HashMap
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        body.add("scope", SCOPE);

        HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> exchange = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (exchange.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = exchange.getBody();
                if (responseBody != null) {
                    String accessToken = (String) responseBody.get("access_token");
                    String refreshToken = (String) responseBody.get("refresh_token");
                    LocalDateTime expiration = LocalDateTime.now().plusSeconds((Integer) responseBody.get("expires_in"));

                    // Użycie accessToken do pobrania nazwy użytkownika
                    String username = fetchUsername(accessToken);

                    // Zapis tokena i nazwy użytkownika w bazie danych
                    Token token = new Token(accessToken, refreshToken, expiration, username);
                    tokenRepository.save(token);

                    return CompletableFuture.completedFuture(ResponseEntity.ok("Token i nazwa użytkownika zostały zapisane."));
                }
            }
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie udało się uzyskać tokena."));
        } catch (Exception e) {
            // Logowanie błędu
            System.err.println("Błąd w createAccessToken: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas generowania tokena."));
        }
    }

    public String fetchUsername(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String userInfoUrl = "https://api.olx.pl/users/me";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // Logowanie całej odpowiedzi
            System.out.println("Odpowiedź z OLX API: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null && body.containsKey("name")) {
                    return (String) body.get("name");
                } else {
                    throw new RuntimeException("Pole 'name' nie istnieje w odpowiedzi.");
                }
            } else {
                throw new RuntimeException("Niepoprawny kod odpowiedzi: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Logowanie błędu
            System.err.println("Błąd podczas pobierani  a nazwy użytkownika: " + e.getMessage());
            throw new RuntimeException("Nie udało się pobrać nazwy użytkownika z OLX API.", e);
        }
    }


}
