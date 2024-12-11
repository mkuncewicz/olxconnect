package com.example.olxconnect.service;

import com.example.olxconnect.entity.Advert;
import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.AdvertRepository;
import com.example.olxconnect.repository.TokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdvertService {

    private static final Logger logger = LoggerFactory.getLogger(AdvertService.class);

    @Autowired
    private AdvertRepository advertRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String ADVERTS_URL = "https://www.olx.pl/api/partner/adverts"; // Endpoint API reklam

    /**
     * Aktualizuje reklamy dla wszystkich tokenów w bazie danych.
     */
    @Scheduled(fixedRate = 43200000)
    public void updateAdverts() {
        // Pobranie wszystkich tokenów z bazy danych
        List<Token> tokens = tokenRepository.findAll();

        logger.info("Update Adverts - used");

        for (Token token : tokens) {
            String accessToken = token.getAccessToken();

            // Pobierz reklamy dla danego tokena z OLX API
            List<Advert> advertsFromApi = fetchAdvertsFromApi(accessToken);

            // Zaktualizuj reklamy w bazie danych
            saveAdverts(advertsFromApi, token);
        }
    }

    /**
     * Pobiera reklamy z OLX API dla danego tokena.
     *
     * @param accessToken token dostępu do OLX API
     * @return lista reklam w formie encji `Advert`
     */
    private List<Advert> fetchAdvertsFromApi(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Version", "2");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    ADVERTS_URL,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);

            // Pobierz listę reklam z klucza "data"
            List<Map<String, Object>> advertsData = (List<Map<String, Object>>) responseBody.get("data");

            // Mapowanie danych z API na encję Advert
            return advertsData.stream()
                    .map(data -> {
                        Advert advert = new Advert();
                        advert.setAdvertId(Long.valueOf(data.get("id").toString()));
                        advert.setUrl((String) data.get("url"));
                        advert.setTitle((String) data.get("title"));
                        return advert;
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas pobierania reklam z OLX API: " + e.getMessage(), e);
        }
    }

    /**
     * Zapisuje listę reklam w bazie danych, powiązując je z danym tokenem.
     *
     * @param adverts lista reklam do zapisania
     * @param token   token, z którym mają być powiązane reklamy
     */
    private void saveAdverts(List<Advert> adverts, Token token) {
        // Usuń istniejące reklamy powiązane z tokenem
        token.getAdverts().clear();

        // Ustaw token jako właściciela i dodaj reklamy do encji Token
        adverts.forEach(advert -> {
            advert.setToken(token);
            token.getAdverts().add(advert);
        });

        // Zapisz token z powiązanymi reklamami
        tokenRepository.save(token);
    }

    public void deleteAdvertsForToken(Token token) {
        token.getAdverts().clear(); // Usunięcie wszystkich reklam z listy
        tokenRepository.save(token); // Zapisanie zmian w encji Token
    }

    public List<Advert> getAdvertsForToken(Token token) {
        return token.getAdverts(); // Pobiera wszystkie reklamy powiązane z tokenem
    }

    public Optional<Advert> getAdvertByAdvertId(Long advertId) {

        return advertRepository.findByAdvertId(advertId);
    }

    public String getTitleAdvert(Optional<Advert> optionalAdvert){

        String advertTitle;

        if (optionalAdvert.isPresent()){
            advertTitle = optionalAdvert.get().getTitle();
        }else {
            advertTitle = "Nie udalo się ustalić reklamy";
        }

        return advertTitle;
    }

    public String getTitleAdvert(Long advertId){

        String advertTitle;

        Optional<Advert> optionalAdvert = advertRepository.findByAdvertId(advertId);

        if (optionalAdvert.isPresent()){
            advertTitle = optionalAdvert.get().getTitle();
        }else {
            advertTitle = "Nie udało się ustalić reklamy";
        }

        return advertTitle;
    }

    public String getAdvertUrl(Long advertId){
        String advertUrl;

        Optional<Advert> optionalAdvert = advertRepository.findByAdvertId(advertId);

        if (optionalAdvert.isPresent()){
            advertUrl = optionalAdvert.get().getUrl();
        }else {
            advertUrl = "Nie udało się pobrać linku do reklamy";
        }

        return advertUrl;
    }
}
