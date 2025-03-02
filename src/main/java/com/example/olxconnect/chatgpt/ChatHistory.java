package com.example.olxconnect.chatgpt;

import com.example.olxconnect.dto.MessageDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatHistory {

    private List<Map<String, String>> messages;

    public ChatHistory() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(String role, String content) {
        messages.add(Map.of(
                "role", role,
                "content", content
        ));
    }

    public List<Map<String, String>> getMessages() {
        return messages;
    }

    public void clearHistory() {
        messages.clear();
    }

    public void loadExternalChatHistory(String externalApiResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(externalApiResponse);
        JsonNode messagesNode = rootNode.path("data");

        for (JsonNode messageNode : messagesNode) {
            String type = messageNode.path("type").asText();
            String text = messageNode.path("text").asText();
            String createdAt = messageNode.path("created_at").asText();

            String role = switch (type) {
                case "received" -> "user";
                case "sent" -> "assistant";
                default -> "user";
            };

            addMessage(role, text);

        }
    }

    public void loadExternalChatHistory(List<MessageDto> messagesList) {
        for (MessageDto message : messagesList) {
            String role = switch (message.getType()) {
                case "received" -> "user";
                case "sent" -> "assistant";
                default -> "user";
            };

            addMessage(role, "[" + message.getCreatedAt() + "] " + message.getText());
        }
    }


}

