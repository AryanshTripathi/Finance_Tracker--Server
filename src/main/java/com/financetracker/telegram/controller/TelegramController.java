package com.financetracker.telegram.controller;

import com.financetracker.telegram.dto.TelegramLinkRequest;
import com.financetracker.telegram.service.TelegramPollingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TelegramController {
    private final TelegramPollingService telegramPollingService;

    public TelegramController(TelegramPollingService telegramPollingService) {
        this.telegramPollingService = telegramPollingService;
    }

    @PostMapping("/telegram/link")
    ResponseEntity<Void> connectWithTelegramBot(@AuthenticationPrincipal UUID userId, @RequestBody TelegramLinkRequest linkPayload) {
        boolean isBotInitialized = telegramPollingService.handleTelegramConnection(userId, linkPayload);
        // Add logging
        return ResponseEntity.noContent().build();
    }
}
