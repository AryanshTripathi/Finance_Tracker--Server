package com.financetracker.telegram.repository;

import com.financetracker.telegram.model.TelegramLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramLinkRepository extends JpaRepository<TelegramLink, String> {
    Optional<TelegramLink> findByCode(String code);
}
