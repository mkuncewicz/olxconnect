package com.example.olxconnect.controller;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.repository.ThreadResponseRepository;
import com.example.olxconnect.service.ChatService;
import com.example.olxconnect.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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

    @GetMapping()
    public String chatPage( @RequestParam("token") String token, @RequestParam("threadId") Long threadId,Model model) {

        String username = chatService.getUsernameByInterlocutorId(token, 1452582100L);

        // Pobieramy wiadomości z serwisu
        List<MessageDto> messages = messageService.getMessages(token, threadId);

        // Przekazujemy je do widoku
        model.addAttribute("chatUserName", username);
        model.addAttribute("messages", messages);

        return "chat";
    }
}
