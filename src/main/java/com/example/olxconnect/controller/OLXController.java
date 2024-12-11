package com.example.olxconnect.controller;

import com.example.olxconnect.service.OlxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class OLXController {

    private final OlxService olxService;

    private static final Logger logger = LoggerFactory.getLogger(OLXController.class);


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
    public String createAccessToken(@RequestParam("code") String code) {
        olxService.createAccessToken(code); // Wywołanie asynchroniczne

        logger.info("Proces uzyskiwania tokena został uruchomiony w tle.");

        return "redirect:/";
    }
}