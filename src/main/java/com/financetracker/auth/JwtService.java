package com.financetracker.auth;

import com.financetracker.exception.InvalidStringUuidConversionException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.expirationMs))
                .signWith(this.getSigningKey()).compact();
    }

    public UUID extractUserId(String token) {
        String subject = Jwts.parser().
                verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new InvalidStringUuidConversionException("string", "uuid");
        }
    }
}
