package com.ziminpro.ums.controllers;

import com.ziminpro.ums.dtos.Constants;
import com.ziminpro.ums.dtos.auth.LoginRequest;
import com.ziminpro.ums.dtos.auth.LoginResponse;
import com.ziminpro.ums.dtos.auth.RegisterRequest;
import com.ziminpro.ums.services.AuthService;
import com.ziminpro.ums.services.GithubAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private GithubAuthService githubAuthService;

    @Value("${github.client-id}")
    private String githubClientId;

    @Value("${github.redirect-uri}")
    private String githubRedirectUri;

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());

        Map<String, Object> response = new HashMap<>();
        if (token == null) {
            response.put(Constants.CODE, "401");
            response.put(Constants.MESSAGE, "Invalid email or password");
            response.put(Constants.DATA, new HashMap<>());
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Login successful");
            response.put(Constants.DATA, new LoginResponse(token));
        }

        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request.getName(), request.getEmail(), request.getPassword());

        Map<String, Object> response = new HashMap<>();
        if (token == null) {
            response.put(Constants.CODE, "400");
            response.put(Constants.MESSAGE, "User not created (email may already be used)");
            response.put(Constants.DATA, new HashMap<>());
        } else {
            response.put(Constants.CODE, "201");
            response.put(Constants.MESSAGE, "User registered successfully");
            response.put(Constants.DATA, new LoginResponse(token));
        }

        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    @GetMapping("/github/login")
    public Mono<ResponseEntity<Void>> githubLogin() {
        String url = UriComponentsBuilder
                .fromUriString("https://github.com/login/oauth/authorize")
                .queryParam("client_id", githubClientId)
                .queryParam("redirect_uri", githubRedirectUri)
                .queryParam("scope", "read:user")
                .build()
                .toUriString();

        return Mono.just(ResponseEntity
                .status(302)
                .header("Location", url)
                .build());
    }

    @GetMapping("/github/callback")
    public Mono<ResponseEntity<Map<String, Object>>> githubCallback(@RequestParam("code") String code) {
        return githubAuthService.handleGithubCallback(code)
                .map(token -> {
                    Map<String, Object> response = new HashMap<>();
                    if (token == null) {
                        response.put(Constants.CODE, "400");
                        response.put(Constants.MESSAGE, "GitHub authentication failed");
                        response.put(Constants.DATA, new HashMap<>());
                    } else {
                        response.put(Constants.CODE, "200");
                        response.put(Constants.MESSAGE, "GitHub login successful");
                        response.put(Constants.DATA, new LoginResponse(token));
                    }
                    return ResponseEntity.ok()
                            .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                            .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                            .body(response);
                });
    }
}