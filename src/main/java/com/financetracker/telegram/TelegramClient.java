package com.financetracker.telegram;

import com.financetracker.telegram.dto.InlineKeyboardMarkup;
import com.financetracker.telegram.dto.TelegramMessageRequest;
import com.financetracker.telegram.dto.TelegramMessageResponse;
import com.financetracker.telegram.dto.TelegramUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TelegramClient {
    private final RestClient restClient;
    private final int DEFAULT_TIMEOUT;

    @Autowired
    public TelegramClient(@Value("${telegram.bot_token}") String botToken) {
        this.DEFAULT_TIMEOUT = 30;
        this.restClient = RestClient.builder().baseUrl("https://api.telegram.org/bot" + botToken).build();
    }
    public TelegramClient(String botToken, int timeout) {
        this.DEFAULT_TIMEOUT = timeout;
        this.restClient = RestClient.builder().baseUrl("https://api.telegram.org/bot" + botToken).build();
    }

    public TelegramMessageResponse sendMessage(Long chat_id, String message, InlineKeyboardMarkup inlineKeyboardDetails) {
        TelegramMessageRequest telegramMessage = new TelegramMessageRequest(chat_id, message, inlineKeyboardDetails);
        TelegramMessageResponse sendMessageResponse = restClient
                .post().uri("/sendMessage").body(telegramMessage).retrieve().body(TelegramMessageResponse.class);
        return sendMessageResponse;
    }

    public TelegramUpdateResponse getUpdate(Long offset) {
        return restClient.get().uri(uriBuilder -> uriBuilder
                .path("/getUpdates")
                .queryParam("offset", offset)
                .queryParam("timeout", this.DEFAULT_TIMEOUT)
                .build()
        ).retrieve().body(TelegramUpdateResponse.class);
    }
}











