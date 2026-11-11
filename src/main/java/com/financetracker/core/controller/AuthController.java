package com.financetracker.core.controller;

import com.financetracker.core.dto.UserCreateRequest;
import com.financetracker.core.dto.UserCreateResponse;
import com.financetracker.core.dto.UserLoginRequest;
import com.financetracker.core.dto.UserLoginResponse;
import com.financetracker.core.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    public final AuthService authService;

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
