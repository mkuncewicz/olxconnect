package com.example.olxconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ThreadResponse {

    private Long id;

    @JsonProperty("advert_id")
    private Long advertId;

    @JsonProperty("interlocutor_id")
    private Long interlocutorId;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("unread_count")
    private Integer unreadCount;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("is_favourite")
    private Boolean isFavourite;
}
