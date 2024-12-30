package com.example.olxconnect.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewMessageMail {

    private String account;

    private Long advertId;

    private String advertTitle;

    private String advertUrl;

    private String time;

    private String accToken;

    private String refreshToken;

    private Long threadId;

    private Long interlocutorId;
}
