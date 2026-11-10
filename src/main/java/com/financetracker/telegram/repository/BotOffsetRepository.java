package com.financetracker.telegram.repository;

import com.financetracker.telegram.model.BotOffset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BotOffsetRepository extends JpaRepository<BotOffset, UUID> {
    Optional<BotOffset> findFirstByOrderByCreatedAtDesc();
}
