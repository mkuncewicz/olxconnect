package com.example.olxconnect.controller;

import com.example.olxconnect.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adverts")
public class AdvertController {

    @Autowired
    private AdvertService advertService;

    @GetMapping("/update")
    public ResponseEntity<String> updateAdverts() {
        advertService.updateAdverts();
        return ResponseEntity.ok("Reklamy zostały zaktualizowane.");
    }

    @GetMapping("/update-by-button")
    public ResponseEntity<Void> updateAdvertsByButton() {
        advertService.updateAdverts();
        return ResponseEntity.ok().build();
    }

}