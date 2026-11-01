package com.financetracker.controller;

import com.financetracker.dto.UserCreateRequest;
import com.financetracker.dto.UserCreateResponse;
import com.financetracker.dto.UserLoginRequest;
import com.financetracker.dto.UserLoginResponse;
import com.financetracker.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    public AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    UserCreateResponse register(@RequestBody UserCreateRequest registrationDetails) {
        return authService.register(registrationDetails);
    }

    @PostMapping("/auth/login")
    UserLoginResponse login(@RequestBody UserLoginRequest loginDetails) {
        return authService.login(loginDetails);
    }
}
