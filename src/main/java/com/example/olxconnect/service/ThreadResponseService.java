package com.example.olxconnect.service;

import com.example.olxconnect.dto.ThreadResponseDto;
import com.example.olxconnect.entity.ThreadResponse;
import com.example.olxconnect.entity.Token;
import com.example.olxconnect.repository.ThreadResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThreadResponseService {

    @Autowired
    private ThreadResponseRepository threadResponseRepository;


    public void saveThreads(List<ThreadResponse> threads, Token owner) {
        for (ThreadResponse thread : threads) {
            thread.setOwner(owner);
            threadResponseRepository.save(thread);
        }
    }

    public void saveUniqueThreads(List<ThreadResponseDto> threadsToSave, Token owner) {

        List<ThreadResponse> threadDBList = threadResponseRepository.findAll();

    }
}
