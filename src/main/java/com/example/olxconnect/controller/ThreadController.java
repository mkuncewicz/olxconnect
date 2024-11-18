package com.example.olxconnect.controller;

import com.example.olxconnect.dto.ThreadResponse;
import com.example.olxconnect.entity.Token;
import com.example.olxconnect.service.OlxService;
import com.example.olxconnect.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class ThreadController {

    private final TokenRepository tokenRepository;
    private final OlxService olxService;

    @Autowired
    public ThreadController(TokenRepository tokenRepository, OlxService olxService) {
        this.tokenRepository = tokenRepository;
        this.olxService = olxService;
    }


    @GetMapping("/threads")
    public String getThreads(Model model, @RequestParam(name = "tokenId") Long tokenId) {
        // Znajdź token dla podanego tokenId
        Token token = tokenRepository.findById(tokenId).orElse(null);

        if (token == null) {
            model.addAttribute("error", "Nie znaleziono użytkownika o podanym tokenId.");
            return "error"; // Możesz stworzyć widok error.html
        }

        // Pobierz wątki użytkownika za pomocą OLX API
        try {
            // Zmień typ listy na List<ThreadResponse>
            List<ThreadResponse> threads = olxService.fetchThreads(token.getAccessToken());

            // Przekaż wątki i dane użytkownika do widoku
            model.addAttribute("username", token.getUsername());
            model.addAttribute("threads", threads);

            return "threads"; // Oczekiwany widok threads.html
        } catch (Exception e) {
            model.addAttribute("error", "Wystąpił błąd podczas pobierania wątków: " + e.getMessage());
            return "error"; // Możesz stworzyć widok error.html
        }
    }
}
