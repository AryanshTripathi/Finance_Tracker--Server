package com.financetracker.telegram;
import org.springframework.web.client.RestClient;

public class TelegramClient {
    private final RestClient restClient;
    private final int DEFAULT_TIMEOUT;

    public TelegramClient(String botToken) {
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











