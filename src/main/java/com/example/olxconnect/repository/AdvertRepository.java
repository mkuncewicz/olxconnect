package com.example.olxconnect.repository;

import com.example.olxconnect.entity.Advert;
import com.example.olxconnect.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvertRepository extends JpaRepository<Advert,Long> {

    void deleteAllByToken(Token token);

    Optional<Advert> findByAdvertId(Long advertId);
}
