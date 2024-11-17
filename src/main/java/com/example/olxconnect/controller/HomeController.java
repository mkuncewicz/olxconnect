package com.example.olxconnect.controller;

import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.TokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private TokenRepository tokenRepository;

    @GetMapping("/")
    public String home(Model model) {

        List<Token> tokenList = tokenRepository.findAll();

        model.addAttribute("tokenList", tokenList);

        return "home";
    }

    @GetMapping("/test")
    public ResponseEntity<Void> test(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 1;
                while (true) {
                    System.out.println("Test: " + i);
                    i++;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();

        return  ResponseEntity.ok().build();
    }
}