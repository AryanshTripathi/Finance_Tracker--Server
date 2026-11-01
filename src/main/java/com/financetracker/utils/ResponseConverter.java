package com.financetracker.utils;

import com.financetracker.dto.UserCreateResponse;
import com.financetracker.dto.UserLoginResponse;
import com.financetracker.model.User;

public final class ResponseConverter {
    private ResponseConverter() {}

    public static UserCreateResponse toUserCreateResponse(User user) {
        return toUserCreateResponse(user, "User Registration Successful");
    }


    public static UserCreateResponse toUserCreateResponse(User user, String responseMessage) {
        return new UserCreateResponse(
            responseMessage,
            user.getEmail(),
            user.getDisplayName()
        );
    }

    public static UserLoginResponse toUserLoginResponse(String token) {
        return new UserLoginResponse(token);
    }
}
