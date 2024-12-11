package com.example.olxconnect.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatComponentsDto {
    private String avatar;
    private String userName;
    private List<MessageDto> messages;
}
