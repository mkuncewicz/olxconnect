package com.example.olxconnect.repository;

import com.example.olxconnect.entity.ThreadResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadResponseRepository extends JpaRepository<ThreadResponse,Long> {

    List<ThreadResponse> findAllByOwnerId(Long ownerId);
}
