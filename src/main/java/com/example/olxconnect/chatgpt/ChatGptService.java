package com.example.olxconnect.chatgpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatGptService {
    private final Assistant assistant;
    private WebClient webClient;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${chatgpt.api.key}")
    private String apiKey;

    public ChatGptService() {
        this.assistant = Assistant.getInstance();
    }

    /** Tworzenie WebClient po inicjalizacji beana */
    @PostConstruct
    private void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl(API_URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getResponse(ChatHistory chatHistory) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> messages = new ArrayList<>();

        // Dodanie kontekstu asystenta
        messages.add(Map.of(
                "role", "system",
                "content", assistant.getModelForApi()
        ));

        // Usunięcie znaczników czasu z wiadomości
        List<Map<String, String>> cleanedMessages = chatHistory.getMessages().stream()
                .map(msg -> Map.of(
                        "role", msg.get("role"),
                        "content", msg.get("content").replaceAll("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] ", "")
                ))
                .toList();

        messages.addAll(cleanedMessages);

        if (messages.isEmpty()) {
            return "Brak historii rozmowy. Nie mogę wygenerować odpowiedzi.";
        }

        // Budowanie JSON dla OpenAI API
        String jsonRequest;
        try {
            jsonRequest = mapper.writeValueAsString(Map.of(
                    "model", "gpt-4",
                    "messages", messages,
                    "temperature", 0.7
            ));
        } catch (Exception e) {
            return "❌ Błąd przy budowaniu zapytania: " + e.getMessage();
        }

        // Wysyłanie zapytania do OpenAI
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey) // Ustawienie tokena dynamicznie
                .bodyValue(jsonRequest)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> parseResponse(response, mapper))
                .block();
    }

    /** Metoda pomocnicza do parsowania JSON */
    private String parseResponse(String response, ObjectMapper mapper) {
        try {
            JsonNode jsonNode = mapper.readTree(response);
            JsonNode choices = jsonNode.path("choices");

            if (choices.isEmpty()) {
                return "❌ Brak odpowiedzi z API.";
            }

            return choices.get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "❌ Błąd przy parsowaniu odpowiedzi API: " + e.getMessage();
        }
    }
}
