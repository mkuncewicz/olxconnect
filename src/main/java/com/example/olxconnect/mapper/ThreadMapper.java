package com.example.olxconnect.mapper;

import com.example.olxconnect.dto.ThreadResponseDto;
import com.example.olxconnect.entity.ThreadResponse;
import com.example.olxconnect.entity.Token;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThreadMapper {


    public ThreadResponse toEntity(ThreadResponseDto dto, Token owner) {

        ThreadResponse entity = new ThreadResponse();
        entity.setThreadId(dto.getId());
        entity.setOwner(owner);
        entity.setAdvertId(dto.getAdvertId());
        entity.setInterlocutorId(dto.getInterlocutorId());
        entity.setTotalCount(dto.getTotalCount());
        entity.setUnreadCount(dto.getUnreadCount());

        return entity;
    }

    public List<ThreadResponse> toListEntity(List<ThreadResponseDto> dtos, Token owner) {

        List<ThreadResponse> entities = new ArrayList<>();

        for (ThreadResponseDto dto : dtos) {
            entities.add(toEntity(dto, owner));
        }

        return entities;
    }

    // Mapowanie z ThreadResponse na ThreadResponseDto
    public ThreadResponseDto toDto(ThreadResponse entity) {
        ThreadResponseDto dto = new ThreadResponseDto();
        dto.setId(entity.getThreadId());
        dto.setAdvertId(entity.getAdvertId());
        dto.setInterlocutorId(entity.getInterlocutorId());
        dto.setTotalCount(entity.getTotalCount());
        dto.setUnreadCount(entity.getUnreadCount());
        // Pola takie jak createdAt i isFavourite będą dodane do entity w razie potrzeby
        return dto;
    }
}
