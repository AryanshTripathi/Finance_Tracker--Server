package com.financetracker.gmail;


import com.financetracker.gmail.dto.GmailTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.UUID;

@Component
public class GmailClient {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private final RestClient restClient = RestClient.builder().build();

    public GmailClient(@Value("${gmail.client-id}") String clientId,
                       @Value("${gmail.client-secret}") String clientSecret,
                       @Value("${gmail.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.clientSecret = clientSecret;
    }

    public String buildAuthorizationUrl(UUID state) {
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public GmailTokenResponse exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("code", code);
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("redirect_uri", redirectUri);
        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(GmailTokenResponse.class);
    }
}
