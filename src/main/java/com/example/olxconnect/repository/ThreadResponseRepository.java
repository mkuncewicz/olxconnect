package com.example.olxconnect.repository;

import com.example.olxconnect.entity.ThreadResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadResponseRepository extends JpaRepository<ThreadResponse,Long> {

    List<ThreadResponse> findAllByOwnerId(Long ownerId);

    void deleteAllByOwnerId(Long ownerId);

    Optional<ThreadResponse> findByThreadId(Long threadId);
}
