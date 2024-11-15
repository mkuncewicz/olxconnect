package com.example.olxconnect.service;

import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class OlxService {

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



    public ResponseEntity<String> createAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("redirect_uri", redirectUri);
        body.put("code", code);
        body.put("scope", SCOPE);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
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

                return ResponseEntity.ok("Token i nazwa użytkownika zostały zapisane.");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie udało się uzyskać tokena.");
    }



    public String fetchUsername(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String userInfoUrl = "https://api.olx.pl/users/me"; // URL endpointa

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            // Pobranie nazwy użytkownika z odpowiedzi
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("name")) {
                return (String) body.get("name");
            }
        }

        throw new RuntimeException("Nie udało się pobrać nazwy użytkownika z OLX API.");
    }
}