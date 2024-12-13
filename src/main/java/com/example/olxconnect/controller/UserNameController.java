package com.example.olxconnect.controller;

import com.example.olxconnect.dto.UserDto;
import com.example.olxconnect.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserNameController {

    @Autowired
    private ChatService chatService;


    @GetMapping
    public String chatPage(@RequestParam("token") String token, @RequestParam("userId") Long userId, Model model) {
        // Pobierz nazwę użytkownika na podstawie ID
        String usernameByInterlocutorId = chatService.getUsernameByInterlocutorId(token, userId);

        // Stwórz obiekt DTO użytkownika i dodaj do modelu
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName(usernameByInterlocutorId);

        model.addAttribute("user", userDto);

        // Zwróć nazwę widoku
        return "user";
    }

}
