package com.ziminpro.ums.services;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Service
public class GithubAuthService {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    @Autowired
    private UmsRepository umsRepository;

    @Autowired
    private AuthService authService;

    private final WebClient webClient = WebClient.builder().build();

    public Mono<String> handleGithubCallback(String code) {
        return getAccessToken(code)
                .flatMap(this::getGithubUser)
                .flatMap(this::findOrCreateUserAndGetJwt);
    }

    private Mono<String> getAccessToken(String code) {
        return webClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (String) body.get("access_token"));
    }

    @SuppressWarnings("unchecked")
    private Mono<Map<String, Object>> getGithubUser(String accessToken) {
        return webClient.get()
                .uri("https://api.github.com/user")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> (Map<String, Object>) m);
    }

    private Mono<String> findOrCreateUserAndGetJwt(Map<String, Object> githubUser) {
        String login = (String) githubUser.get("login");
        String email = (String) githubUser.get("email");
        if (email == null || email.isBlank()) {
            email = login + "@github.local";
        }
        String finalEmail = email;
        String finalLogin = login;

        return Mono.fromCallable(() -> {
            User existing = umsRepository.findUserByEmail(finalEmail);

            if (existing.getId() != null) {
                return authService.login(finalEmail, existing.getPassword());
            }

            // создаём нового пользователя
            User user = new User();
            user.setName(finalLogin);
            user.setEmail(finalEmail);
            user.setPassword("github-oauth");
            user.setCreated((int) Instant.now().getEpochSecond());

            Roles subscriberRole = new Roles();
            subscriberRole.setRole("SUBSCRIBER");
            user.setRoles(Collections.singletonList(subscriberRole));

            var userId = umsRepository.createUser(user);
            if (userId == null) {
                return null;
            }

            user.setId(userId);
            return authService.login(finalEmail, user.getPassword());
        });
    }
}