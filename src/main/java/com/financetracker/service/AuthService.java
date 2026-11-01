package com.financetracker.service;

import com.financetracker.auth.JwtService;
import com.financetracker.dto.UserCreateRequest;
import com.financetracker.dto.UserCreateResponse;
import com.financetracker.dto.UserLoginRequest;
import com.financetracker.dto.UserLoginResponse;
import com.financetracker.exception.IncorrectPasswordException;
import com.financetracker.exception.UserAlreadyRegisteredException;
import com.financetracker.exception.UserNotRegisteredException;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.utils.ResponseConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;


    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserCreateResponse register(UserCreateRequest userInfo) {
        boolean isUserRegistered = userRepository.existsByEmail(userInfo.email());
        if(isUserRegistered) {
            throw new UserAlreadyRegisteredException("Email " + userInfo.email() + " already exists. Please login");
        }

        User userDetails = new User();
        userDetails.setDisplayName(userInfo.displayName());
        userDetails.setEmail(userInfo.email());
        userDetails.setPasswordHash(passwordEncoder.encode(userInfo.plainPassword()));

        userRepository.save(userDetails);
        return ResponseConverter.toUserCreateResponse(userDetails);
    }

    public UserLoginResponse login(UserLoginRequest loginDetails) {
        User user = userRepository.findByEmail(loginDetails.email())
            .orElseThrow(() -> new UserNotRegisteredException(
                "Email " + loginDetails.email() + " does not exist. Please register"
            ));

        boolean isPasswordCorrect = passwordEncoder.matches(loginDetails.password(), user.getPasswordHash());
        if(!isPasswordCorrect) {
            throw new IncorrectPasswordException("Incorrect Password while login");
        }

        return ResponseConverter.toUserLoginResponse(jwtService.generateToken(user.getId()));
    }
}

