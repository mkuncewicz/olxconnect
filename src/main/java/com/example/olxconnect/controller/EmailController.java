package com.example.olxconnect.controller;

import com.example.olxconnect.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-email")
    public String sendEmail() {
        emailService.sendSimpleEmail("", "Testowy temat", "Treść testowego e-maila.");
        return "E-mail został wysłany!";
    }
}
