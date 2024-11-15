package com.example.olxconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OlxconnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(OlxconnectApplication.class, args);
    }

}
