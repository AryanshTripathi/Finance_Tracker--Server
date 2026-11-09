package com.financetracker.repository;

import com.financetracker.model.TelegramLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramLinkRepository extends JpaRepository<TelegramLink, String> {
}
