package com.example.olxconnect.controller;

import com.example.olxconnect.dto.ChatComponentsDto;
import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.entity.ThreadResponse;
import com.example.olxconnect.repository.ThreadResponseRepository;
import com.example.olxconnect.service.ChatService;
import com.example.olxconnect.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ThreadResponseRepository threadResponseRepository;

    @GetMapping("/{token}/{threadId}")
    public String chatPage(Model model, @PathVariable String token, @PathVariable Long threadId) {
        try {
            // Pobranie informacji o wątku z bazy danych
            Optional<ThreadResponse> optionalThreadResponse = threadResponseRepository.findByThreadId(threadId);

            ChatComponentsDto chatComponents;

            // Jeśli wątek istnieje w bazie, pobierz jego komponenty
            if (optionalThreadResponse.isPresent()) {
                ThreadResponse threadResponse = optionalThreadResponse.get();
                chatComponents = chatService.getChatComponents(token, threadId, threadResponse.getInterlocutorId());
            } else {
                // Jeśli wątku nie ma w bazie, pobierz tylko wiadomości
                chatComponents = new ChatComponentsDto();
                List<MessageDto> messages = messageService.getMessages(token, threadId);
                chatComponents.setMessages(messages);
                chatComponents.setUserName("Brak nazwy");
            }

            // Dodanie atrybutów do modelu
            model.addAttribute("chatUserName", chatComponents.getUserName());
            model.addAttribute("messages", chatComponents.getMessages());

            return "chat";

        } catch (Exception e) {
            // Obsługa błędów
            model.addAttribute("error", "Wystąpił błąd podczas ładowania czatu: " + e.getMessage());
            return "error";
        }
    }
}
