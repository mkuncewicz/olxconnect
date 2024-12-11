package com.example.olxconnect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Ignoruje pola, które nie są zdefiniowane w DTO
public class MessageDto {
    private Long id;

    @JsonProperty("thread_id")
    private Long threadId;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String type; // "sent" lub "received"
    private String text;

    @JsonProperty("is_read")
    private boolean isRead;

    private List<Attachment> attachments = List.of();
    private List<Cv> cvs = List.of();
    private String phone;
}
