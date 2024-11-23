package com.example.olxconnect.service;

import com.example.olxconnect.dto.ThreadResponseDto;
import com.example.olxconnect.entity.ThreadResponse;
import com.example.olxconnect.entity.Token;
import com.example.olxconnect.mail.NewMessageMail;
import com.example.olxconnect.mapper.ThreadMapper;
import com.example.olxconnect.repository.TokenRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private static final String USER_INFO_URL = "https://api.olx.pl/users/me";
    private static final String THREADS_URL = "https://www.olx.pl/api/partner/threads";


    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ThreadResponseService threadResponseService;

    @Autowired
    private AdvertService advertService;

    @Autowired
    private ThreadMapper threadMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmailService emailService;

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

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

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

                    logger.debug("Token dostępu: {}", accessToken);

                    // Pobranie nazwy użytkownika
                    String username = fetchUsername(accessToken);
//                    String username = "TestName1";

                    // Zapisanie tokena w bazie danych
                    Token token = new Token(accessToken, refreshToken, expiration, username);
                    tokenRepository.save(token);

                    return CompletableFuture.completedFuture(ResponseEntity.ok("Token i użytkownik zapisani."));
                }
            }
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie udało się uzyskać tokena."));
        } catch (HttpClientErrorException e) {
            logger.error("Błąd HTTP w createAccessToken: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd HTTP: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Ogólny błąd w createAccessToken: ", e);
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Wystąpił błąd podczas generowania tokena."));
        }
    }

    public String fetchUsername(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Version", "2");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String userInfoUrl = "https://www.olx.pl/api/partner/users/me";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Logowanie całej odpowiedzi
            logger.info("Odpowiedź HTTP: {}", response);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Parsowanie JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);

                // Pobranie zagnieżdżonego pola "data"
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data == null) {
                    throw new RuntimeException("Pole 'data' nie istnieje w odpowiedzi: " + body);
                }

                // Pobranie pola "name" lub "email"
                String name = (String) data.get("name");
                if (name != null && !name.isEmpty()) {
                    return name;
                }

                String email = (String) data.get("email");
                if (email != null && !email.isEmpty()) {
                    return email;
                }

                throw new RuntimeException("Pole 'name' oraz 'email' nie istnieje w odpowiedzi: " + data);

            } else {
                throw new RuntimeException("Niepoprawny kod odpowiedzi: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Błąd HTTP w fetchUsername: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Błąd HTTP podczas pobierania nazwy użytkownika: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Ogólny błąd w fetchUsername: ", e);
            throw new RuntimeException("Nie udało się pobrać nazwy użytkownika z OLX API.", e);
        }
    }


    public List<ThreadResponseDto> fetchThreads(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Version", "2");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String threadsUrl = "https://www.olx.pl/api/partner/threads";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    threadsUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Logowanie odpowiedzi dla debugowania
            logger.info("Odpowiedź z OLX Threads API: {}", response.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);

            // Zakładamy, że odpowiedź jest opakowana w klucz "data", jeśli nie, usuń ten krok
            List<ThreadResponseDto> threads = objectMapper.convertValue(
                    responseBody.get("data"),
                    new TypeReference<List<ThreadResponseDto>>() {}
            );

            return threads;

        } catch (HttpClientErrorException e) {
            logger.error("Błąd HTTP w fetchThreads: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Błąd HTTP podczas pobierania wątków: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Ogólny błąd w fetchThreads: ", e);
            throw new RuntimeException("Nie udało się pobrać wątków z OLX API.", e);
        }
    }


    public List<NewMessageMail> isNewMessage() {
        List<Token> tokenList = tokenRepository.findAll(); // Pobierz wszystkie tokeny z bazy danych
        List<NewMessageMail> newMessagesList = new ArrayList<>(); // Lista nowych wiadomości

        for (Token token : tokenList) {
            String accessToken = token.getAccessToken(); // Pobierz access token użytkownika
            List<ThreadResponse> tokenThreadsDB = threadResponseService.findAllByOwner(token); // Pobierz wątki z bazy dla tego tokena
            List<ThreadResponseDto> threadsFromAPI = fetchThreads(accessToken); // Pobierz wątki z OLX API

            for (ThreadResponseDto threadDto : threadsFromAPI) {
                // Szukaj odpowiedniego wątku w bazie danych
                ThreadResponse matchingThread = tokenThreadsDB.stream()
                        .filter(t -> t.getThreadId().equals(threadDto.getId()))
                        .findFirst()
                        .orElse(null);

                if (matchingThread == null) {
                    // Jeśli wątek nie istnieje w bazie, zapisz nowy wątek
                    ThreadResponse newThread = threadMapper.toEntity(threadDto, token);
                    threadResponseService.save(newThread); // Użyj ThreadResponseService do zapisu

                    Long advertId = newThread.getAdvertId();
                    String advertTitle = advertService.getTitleAdvert(advertId);

                    // Dodaj informację o nowej wiadomości
                    newMessagesList.add(new NewMessageMail(
                            token.getUsername(),
                            advertTitle,
                            threadDto.getCreatedAt()
                    ));
                } else {
                    boolean isUpdated = false;

                    // Jeśli liczba wiadomości zmieniła się
                    if (!matchingThread.getUnreadCount().equals(threadDto.getUnreadCount())) {
                        matchingThread.setUnreadCount(threadDto.getUnreadCount());
                        isUpdated = true;
                    }

                    if (!matchingThread.getTotalCount().equals(threadDto.getTotalCount())) {
                        matchingThread.setTotalCount(threadDto.getTotalCount());
                        isUpdated = true;
                    }

                    if (isUpdated) {
                        threadResponseService.save(matchingThread); // Nadpisz istniejący wątek w bazie

                        Long advertId = matchingThread.getAdvertId();
                        String advertTitle = advertService.getTitleAdvert(advertId);


                        newMessagesList.add(new NewMessageMail(
                                token.getUsername(),
                                advertTitle, // Możesz pobrać tytuł ogłoszenia, jeśli jest dostępny
                                threadDto.getCreatedAt()
                        ));
                    }

                    // Jeśli liczba nieprzeczytanych wiadomości zmniejszyła się
                    if (matchingThread.getUnreadCount() > threadDto.getUnreadCount()) {
                        matchingThread.setUnreadCount(threadDto.getUnreadCount());
                        threadResponseService.save(matchingThread); // Nadpisz liczbę nieprzeczytanych wiadomości
                    }
                }
            }
        }

        return newMessagesList; // Zwróć listę nowych wiadomości
    }


    @Scheduled(fixedRate = 60000) // Uruchamianie co 60 sekund
    public void checkAndNotifyNewMessages() {
        logger.info("checkAndNotifyNewMessages użyto");

        List<NewMessageMail> newMessagesList = isNewMessage(); // Pobranie nowych wiadomości/wątków

        for (NewMessageMail newMessage : newMessagesList) {
            // Tworzenie treści wiadomości e-mail
            String emailContent = String.format(
                    "Konto: %s\nOgłoszenie: %s\nCzas: %s",
                    newMessage.getAccount(),
                    newMessage.getAdvertTitle(),
                    newMessage.getTime()
            );

            // Wyślij e-mail na określony adres (tu zakładamy, że użytkownik ma powiązany adres e-mail)
            emailService.sendSimpleEmail(
                    "kuncewicz.mateusz@gmail.com", // W rzeczywistości adres może być powiązany z kontem
                    "Nowa wiadomość w OLX",
                    emailContent
            );

            logger.info("Wysłano e-mail do użytkownika: {}", newMessage.getAccount());
        }
    }

}
