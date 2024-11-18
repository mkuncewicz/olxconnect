package com.example.olxconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiration;
    private String username;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThreadResponse> threads = new ArrayList<>();

    public Token(String accessToken, String refreshToken, LocalDateTime expiration, String username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
        this.username = username;
    }
}

