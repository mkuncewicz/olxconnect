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

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress); // Ustaw nadawcę
        message.setTo(to);           // Ustaw odbiorcę
        message.setSubject(subject); // Ustaw temat
        message.setText(text);       // Ustaw treść

        try {
            mailSender.send(message); // Wyślij e-mail
            System.out.println("E-mail został wysłany do: " + to);
        } catch (MailException e) {
            System.err.println("Błąd podczas wysyłania e-maila: " + e.getMessage());
            throw new RuntimeException("Nie udało się wysłać e-maila.", e);
        }
    }
}
