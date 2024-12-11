package com.example.olxconnect.controller;

import com.example.olxconnect.dto.ThreadResponseDto;
import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.TokenRepository;
import com.example.olxconnect.service.OlxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ThreadController {

    private static final Logger logger = LoggerFactory.getLogger(ThreadController.class);

    private final TokenRepository tokenRepository;
    private final OlxService olxService;

    @Autowired
    public ThreadController(TokenRepository tokenRepository, OlxService olxService) {
        this.tokenRepository = tokenRepository;
        this.olxService = olxService;
    }


    @GetMapping("/threads")
    public String getThreads(Model model, @RequestParam(name = "tokenId") Long tokenId) {
        logger.info("Wywołano /threads z parametrem tokenId: {}", tokenId);

        // Znajdź token dla podanego tokenId
        Token token = tokenRepository.findById(tokenId).orElse(null);

        if (token == null) {
            logger.error("Nie znaleziono tokenu o ID: {}", tokenId);
            model.addAttribute("error", "Nie znaleziono użytkownika o podanym tokenId.");
            return "error"; // Możesz stworzyć widok error.html
        }

        // Pobierz wątki użytkownika za pomocą OLX API
        try {
            List<ThreadResponseDto> threads = olxService.fetchThreads(token.getAccessToken());
            logger.info("Pobrano {} wątków dla użytkownika: {}", threads.size(), token.getUsername());

            model.addAttribute("username", token.getUsername());
            model.addAttribute("threads", threads);

            return "threads"; // Oczekiwany widok threads.html
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania wątków dla użytkownika: {}. Szczegóły: ", token.getUsername(), e);
            model.addAttribute("error", "Wystąpił błąd podczas pobierania wątków: " + e.getMessage());
            return "error"; // Możesz stworzyć widok error.html
        }
    }
}
