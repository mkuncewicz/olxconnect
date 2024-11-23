package com.example.olxconnect.controller;

import com.example.olxconnect.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * Endpoint do wysyłania wiadomości e-mail.
     *
     * @param to      adres odbiorcy (opcjonalny, domyślnie do testu)
     * @param subject temat wiadomości (opcjonalny, domyślnie "Testowy temat")
     * @param text    treść wiadomości (opcjonalny, domyślnie "Treść testowego e-maila")
     * @return komunikat o powodzeniu lub błędzie wysyłania e-maila
     */
    @GetMapping("/send-email")
    public String sendEmail(
            @RequestParam(defaultValue = "kuncewicz.mateusz@gmail.com") String to,
            @RequestParam(defaultValue = "Testowy temat") String subject,
            @RequestParam(defaultValue = "Treść testowego e-maila") String text
    ) {
        try {
            emailService.sendSimpleEmail(to, subject, text);
            return "E-mail został wysłany do: " + to;
        } catch (Exception e) {
            return "Błąd podczas wysyłania e-maila: " + e.getMessage();
        }
    }
}
