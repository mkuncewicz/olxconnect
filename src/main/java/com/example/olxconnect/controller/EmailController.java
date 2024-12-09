package com.example.olxconnect.controller;

import com.example.olxconnect.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    /**
     * Endpoint do wysyłania wiadomości e-mail z domyślnym odbiorcą.
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
            emailService.sendEmail(
                    "test@stanislawnowak.pl", // Nadawca (ustawiony jako zweryfikowany w MailerSend)
                    to, // Odbiorca
                    subject,
                    text
            );
            return "E-mail został wysłany do: " + to;
        } catch (Exception e) {
            logger.error("Błąd podczas wysyłania e-maila: {}", e);
            return "Błąd podczas wysyłania e-maila: " + e.getMessage();
        }
    }


    /**
     * Endpoint do wysyłania wiadomości e-mail z dynamicznym odbiorcą.
     *
     * @param to adres odbiorcy (wymagany)
     * @return komunikat o powodzeniu lub błędzie wysyłania e-maila
     */
    @GetMapping("/send-email-recipient")
    public String sendEmailRecipient(@RequestParam String to) {
        try {
            String subject = "Domyślny temat";
            String text = "Domyślna treść wiadomości.";

            emailService.sendEmail(
                    "test@stanislawnowak.pl", // Nadawca (stały)
                    to, // Dynamiczny odbiorca
                    subject,
                    text
            );

            return "E-mail został wysłany do: " + to;
        } catch (Exception e) {
            logger.error("Błąd podczas wysyłania e-maila: {}", e);
            return "Błąd podczas wysyłania e-maila: " + e.getMessage();
        }
    }

}
