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

        Optional<ThreadResponse> optionalThreadResponse = threadResponseRepository.findByThreadId(threadId);

        ChatComponentsDto chatComponents;

        if (optionalThreadResponse.isPresent()) {
            chatComponents = chatService.getChatComponents(token, threadId, optionalThreadResponse.get().getInterlocutorId());
        }else {
            chatComponents = new ChatComponentsDto();
            List<MessageDto> messages = messageService.getMessages(token, threadId);

            chatComponents.setMessages(messages);
            chatComponents.setUserName("Brak nazwy");
        }

        model.addAttribute("chatUserName", chatComponents.getUserName()); // Przykładowa nazwa użytkownika
        model.addAttribute("messages", chatComponents.getMessages());
        return "chat";
    }

}
