package com.example.olxconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatComponentsDto {
    private String avatar;
    private String userName;
    private List<MessageDto> messages;
}
