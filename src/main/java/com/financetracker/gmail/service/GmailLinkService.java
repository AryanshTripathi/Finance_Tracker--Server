package com.financetracker.gmail.service;

import com.financetracker.exception.CodeExpiredException;
import com.financetracker.exception.OAuthStateNotFoundException;
import com.financetracker.gmail.GmailClient;
import com.financetracker.gmail.dto.GmailTokenResponse;
import com.financetracker.gmail.model.GmailLink;
import com.financetracker.gmail.model.OAuthToken;
import com.financetracker.gmail.repository.GmailLinkRepository;
import com.financetracker.gmail.repository.OAuthTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class GmailLinkService {
    private final GmailLinkRepository gmailLinkRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final GmailClient gmailClient;

    public GmailLinkService(GmailLinkRepository gmailLinkRepository, GmailClient gmailClient, OAuthTokenRepository oAuthTokenRepository) {
        this.gmailLinkRepository = gmailLinkRepository;
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.gmailClient = gmailClient;
    }

    public String initiateLink(UUID userId) {
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);
        GmailLink linkRequest = new GmailLink(userId, expiresAt);
        gmailLinkRepository.save(linkRequest);
        return gmailClient.buildAuthorizationUrl(linkRequest.getId());
    }

    public void handleCallback(UUID state, String code) {
        GmailLink gmailLink = gmailLinkRepository.findById(state).orElseThrow(() -> new OAuthStateNotFoundException(state));
        UUID userId = gmailLink.getUserId();

        if(gmailLink.getExpiresAt().isAfter(Instant.now())) {
            GmailTokenResponse response = gmailClient.exchangeCodeForTokens(code);
            OAuthToken oAuthToken = oAuthTokenRepository.findByUserId(userId).orElse(new OAuthToken(userId, null));
            oAuthToken.setOAuthRefreshToken(response.refreshToken());
            oAuthTokenRepository.save(oAuthToken);
            gmailLinkRepository.delete(gmailLink);
        } else {
            gmailLinkRepository.delete(gmailLink);
            throw new CodeExpiredException("Gmail Auth Link Expired. Please try again");
        }
    }
}
