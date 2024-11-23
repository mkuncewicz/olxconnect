package com.example.olxconnect.controller;

import com.example.olxconnect.service.OlxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/auth")
public class OLXController {

    private final OlxService olxService;

    @Autowired
    public OLXController(OlxService olxService) {
        this.olxService = olxService;
    }

    @GetMapping("/login")
    public String loginOLX() {
        // Przekierowanie do wygenerowanego URL autoryzacji OLX
        return "redirect:" + olxService.getAuthorizationUrl();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> createAccessToken(@RequestParam("code") String code) {
        olxService.createAccessToken(code); // Wywołanie asynchroniczne
        return ResponseEntity.ok("Proces uzyskiwania tokena został uruchomiony w tle.");
    }
}