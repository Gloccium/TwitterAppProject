package com.ziminpro.twitter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    /**
     * Возвращает subject (userId) из токена.
     * Я не проверяю подпись (т.к. проект учебный)
     */
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .build()
                    .parseClaimsJwt(stripSignature(token))
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private String stripSignature(String token) {
        int lastDot = token.lastIndexOf('.');
        if (lastDot > 0) {
            return token.substring(0, lastDot + 1);
        }
        return token;
    }
}