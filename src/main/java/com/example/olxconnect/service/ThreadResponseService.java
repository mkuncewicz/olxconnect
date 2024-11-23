package com.example.olxconnect.service;

import com.example.olxconnect.dto.ThreadResponseDto;
import com.example.olxconnect.entity.ThreadResponse;
import com.example.olxconnect.entity.Token;
import com.example.olxconnect.mapper.ThreadMapper;
import com.example.olxconnect.repository.ThreadResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadResponseService {

    @Autowired
    private ThreadResponseRepository threadResponseRepository;

    @Autowired
    private ThreadMapper threadMapper;


    public List<ThreadResponse> findAllByOwner(Token owner) {
        return threadResponseRepository.findAllByOwnerId(owner.getId());
    }


    public void save(ThreadResponse thread) {
        threadResponseRepository.save(thread);
    }


    public void deleteAllByOwner(Token owner) {
        threadResponseRepository.deleteAllByOwnerId(owner.getId());
    }


    public void saveThreads(List<ThreadResponse> threads, Token owner) {
        threadResponseRepository.deleteAllByOwnerId(owner.getId());

        for (ThreadResponse thread : threads) {
            thread.setOwner(owner);
            threadResponseRepository.save(thread);
        }
    }


    public void saveUniqueThreads(List<ThreadResponseDto> threadsToSave, Token owner) {
        List<ThreadResponse> threadDBList = threadResponseRepository.findAllByOwnerId(owner.getId());

        for (ThreadResponseDto threadDto : threadsToSave) {

            ThreadResponse existingThread = threadDBList.stream()
                    .filter(t -> t.getThreadId().equals(threadDto.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingThread == null) {

                ThreadResponse newThread = threadMapper.toEntity(threadDto, owner);
                threadResponseRepository.save(newThread);
            } else {

                boolean updated = false;

                if (!existingThread.getTotalCount().equals(threadDto.getTotalCount())) {
                    existingThread.setTotalCount(threadDto.getTotalCount());
                    updated = true;
                }

                if (!existingThread.getUnreadCount().equals(threadDto.getUnreadCount())) {
                    existingThread.setUnreadCount(threadDto.getUnreadCount());
                    updated = true;
                }

                if (updated) {
                    threadResponseRepository.save(existingThread);
                }
            }
        }
    }


    public List<ThreadResponseDto> findNewOrUpdatedThreads(Token owner, List<ThreadResponseDto> threadsFromAPI) {
        List<ThreadResponse> threadDBList = threadResponseRepository.findAllByOwnerId(owner.getId());
        List<ThreadResponseDto> newOrUpdatedThreads = new ArrayList<>();

        for (ThreadResponseDto threadDto : threadsFromAPI) {
            ThreadResponse matchingThread = threadDBList.stream()
                    .filter(t -> t.getThreadId().equals(threadDto.getId()))
                    .findFirst()
                    .orElse(null);

            if (matchingThread == null ||
                    !matchingThread.getTotalCount().equals(threadDto.getTotalCount()) ||
                    !matchingThread.getUnreadCount().equals(threadDto.getUnreadCount())) {
                newOrUpdatedThreads.add(threadDto);
            }
        }

        return newOrUpdatedThreads;
    }
}

