package com.example.olxconnect.controller;

import com.example.olxconnect.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshTokensManually() {
        tokenService.refreshTokens(); // Wywołanie metody serwisowej
        return ResponseEntity.ok("Tokeny odświeżone!");
    }
}
