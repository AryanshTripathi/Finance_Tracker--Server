package com.financetracker.gmail.controller;

import com.financetracker.gmail.service.GmailLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class GmailLinkController {
    private final GmailLinkService gmailLinkService;

    public GmailLinkController(GmailLinkService gmailLinkService) {
        this.gmailLinkService = gmailLinkService;
    }

    @PostMapping("/gmail/link/init")
    ResponseEntity<Map<String, String>> initiateGmailLink(@AuthenticationPrincipal UUID userId) {
        String authorizationUrl = gmailLinkService.initiateLink(userId);
        return ResponseEntity.ok(Map.of("authorizationUrl", authorizationUrl));
    }

    @GetMapping("/gmail/callback")
    ResponseEntity<String> handleOauthCallback(
            @RequestParam UUID state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error)
    {
        if(error != null) {
            return ResponseEntity.ok("Gmail linking was cancelled.");
        }

        gmailLinkService.handleCallback(state, code);
        return ResponseEntity.ok("Gmail account linked successfully! You can close this tab.");
    }
}
