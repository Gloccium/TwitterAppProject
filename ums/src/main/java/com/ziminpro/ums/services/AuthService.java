package com.ziminpro.ums.services;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

@Service
public class AuthService {

    @Autowired
    private UmsRepository umsRepository;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String login(String email, String password) {
        User user = umsRepository.findUserByEmail(email);
        if (user.getId() == null) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        return generateToken(user);
    }

    public String register(String name, String email, String password) {
        User existing = umsRepository.findUserByEmail(email);
        if (existing.getId() != null) {
            return null;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreated((int) Instant.now().getEpochSecond());

        Roles subscriberRole = new Roles();
        subscriberRole.setRole("SUBSCRIBER");
        user.setRoles(Collections.singletonList(subscriberRole));

        var userId = umsRepository.createUser(user);
        if (userId == null) {
            return null;
        }

        user.setId(userId);
        return generateToken(user);
    }

    private String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600))) // 1 час
                .signWith(key)
                .compact();
    }

    public Key getKey() {
        return key;
    }
}