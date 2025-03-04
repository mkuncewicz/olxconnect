package com.example.olxconnect.controller;

import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private TokenRepository tokenRepository;

    @GetMapping("/")
    public String home(Model model) {

        List<Token> tokenList = tokenRepository.findAll();

        model.addAttribute("tokenList", tokenList);

        return "home.html";
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (tokenRepository.existsById(id)) {
            tokenRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}