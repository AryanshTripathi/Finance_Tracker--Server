package com.financetracker.core.controller;

import com.financetracker.core.dto.TransactionIngestedEvent;
import com.financetracker.core.service.RabbitmqService;
import com.financetracker.telegram.service.TelegramPollingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
    private final TelegramPollingService telegramPollingService;
    private final RabbitmqService rabbitmqService;

    DebugController(RabbitmqService rabbitmqService, TelegramPollingService telegramPollingService) {
        this.telegramPollingService = telegramPollingService;
        this.rabbitmqService = rabbitmqService;
    }

    @PostMapping("/debug/publish")
    void publishRabbitmqMessage(@RequestBody TransactionIngestedEvent eventPayload) {
        rabbitmqService.publishIngestedTransaction(eventPayload);
    }
}
