package com.financetracker.core.service;

import com.financetracker.config.RabbitmqConfig;
import com.financetracker.core.dto.TransactionIngestedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitmqService {
    private final RabbitTemplate rabbitTemplate;

    RabbitmqService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishIngestedTransaction(TransactionIngestedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitmqConfig.TRANSACTIONS_EXCHANGE,
                RabbitmqConfig.GMAIL_INGESTED_ROUTING_KEY,
                event);
    }
}
