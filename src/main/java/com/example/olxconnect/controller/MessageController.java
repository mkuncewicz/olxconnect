package com.example.olxconnect.controller;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/messages")
    public String getMessages(
            @RequestParam("token") String token,
            @RequestParam("threadId") Long threadId,
            Model model
    ) {
        try {
            // Pobieramy wiadomości z serwisu
            List<MessageDto> messages = messageService.getMessages(token, threadId);

            // Przekazujemy je do widoku
            model.addAttribute("messages", messages);
            return "messages";

        } catch (Exception e) {
            model.addAttribute("error", "Błąd podczas pobierania wiadomości: " + e.getMessage());
            return "error";
        }
    }
}
