package com.financetracker.gmail.repository;

import com.financetracker.gmail.model.GmailLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GmailLinkRepository extends JpaRepository<GmailLink, UUID> {
}
