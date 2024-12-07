package com.example.olxconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Użyj wartości z konfiguracji, jeśli istnieje (plik application.yml)
    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Wysyła prosty e-mail z podanym tematem i treścią.
     *
     * @param to      Adres odbiorcy
     * @param subject Temat wiadomości
     * @param text    Treść wiadomości
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        // Ustaw nadawcę na podstawie konfiguracji
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        } else {
            throw new IllegalStateException("Adres nadawcy (from) nie został skonfigurowany!");
        }

        try {
            mailSender.send(message);
            System.out.println("E-mail wysłany do: " + to);
        } catch (MailException e) {
            System.err.println("Błąd podczas wysyłania e-maila: " + e.getMessage());
            throw new RuntimeException("Nie udało się wysłać e-maila.", e);
        }
    }
}
