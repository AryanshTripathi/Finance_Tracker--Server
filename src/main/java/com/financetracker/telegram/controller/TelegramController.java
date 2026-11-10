package com.financetracker.telegram.controller;

import com.financetracker.core.model.User;
import com.financetracker.core.repository.UserRepository;
import com.financetracker.exception.UserNotRegisteredException;
import com.financetracker.telegram.dto.TelegramLinkRequest;
import com.financetracker.telegram.model.TelegramLink;
import com.financetracker.telegram.repository.TelegramLinkRepository;
import com.financetracker.telegram.service.TelegramPollingService;
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
    public boolean connectWithTelegramBot(@AuthenticationPrincipal UUID userId, @RequestBody TelegramLinkRequest linkPayoad) {
        return telegramPollingService.handleTelegramConnection(userId, linkPayoad);
    }
}
