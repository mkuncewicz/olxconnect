package com.example.olxconnect.controller;

import com.example.olxconnect.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PatchMapping("/{tokenId}/username")
    public ResponseEntity<String> updateUserName(@PathVariable Long tokenId, @RequestBody Map<String, String> request) {
        String newUsername = request.get("username");

        if (newUsername == null || newUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nazwa użytkownika nie może być pusta.");
        }

        tokenService.updateUserName(tokenId, newUsername);
        return ResponseEntity.ok("Nazwa użytkownika została zaktualizowana.");
    }
}
