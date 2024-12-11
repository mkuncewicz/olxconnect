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
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = Logger.getLogger(ChatController.class.getName());

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ThreadResponseRepository threadResponseRepository;

    @GetMapping("/{token}/{threadId}")
    public String chatPage(Model model, @PathVariable String token, @PathVariable Long threadId) {
        if (token == null || token.isBlank() || threadId == null) {
            logger.warning("Nieprawidłowy token lub ID wątku.");
            model.addAttribute("error", "Nieprawidłowy token lub ID wątku.");
            return "error";
        }

        try {
            // Pobranie informacji o wątku z bazy danych
            Optional<ThreadResponse> optionalThreadResponse = threadResponseRepository.findByThreadId(threadId);

            ChatComponentsDto chatComponents;

            if (optionalThreadResponse.isPresent()) {
                ThreadResponse threadResponse = optionalThreadResponse.get();
                logger.info("Wątek znaleziony w bazie danych: " + threadResponse.getThreadId());
                chatComponents = chatService.getChatComponents(token, threadId, threadResponse.getInterlocutorId());
            } else {
                logger.warning("Wątek o ID " + threadId + " nie istnieje w bazie. Pobieranie tylko wiadomości.");
                chatComponents = new ChatComponentsDto();
                List<MessageDto> messages = messageService.getMessages(token, threadId);
                chatComponents.setMessages(messages);
                chatComponents.setUserName("Brak nazwy");
            }

            // Dodanie atrybutów do modelu
            model.addAttribute("chatUserName", chatComponents.getUserName());
            model.addAttribute("messages", chatComponents.getMessages());

            logger.info("Załadowano dane czatu dla wątku o ID " + threadId);
            return "chat";

        } catch (Exception e) {
            // Logowanie błędu
            logger.log(Level.SEVERE, "Błąd podczas ładowania czatu dla wątku o ID " + threadId, e);
            model.addAttribute("error", "Wystąpił błąd podczas ładowania czatu: " + e.getMessage());
            return "error";
        }
    }
}
