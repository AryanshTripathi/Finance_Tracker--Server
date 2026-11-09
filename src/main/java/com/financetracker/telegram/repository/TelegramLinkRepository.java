package com.financetracker.telegram.repository;

import com.financetracker.telegram.model.TelegramLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramLinkRepository extends JpaRepository<TelegramLink, String> {
}
