package com.example.olxconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ThreadResponse {
    private Long id;
    private Long advertId;
    private Long interlocutorId;
    private Integer totalCount;
    private Integer unreadCount;
    private String createdAt;
    private Boolean isFavourite;

}
