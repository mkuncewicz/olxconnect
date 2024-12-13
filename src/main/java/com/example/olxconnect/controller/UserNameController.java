package com.example.olxconnect.controller;

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


    @GetMapping()
    public String chatPage(@RequestParam("token") String token,@RequestParam("userId") Long userId, Model model) {

        String usernameByInterlocutorId = chatService.getUsernameByInterlocutorId(token, userId);

        return usernameByInterlocutorId;
    }
}
