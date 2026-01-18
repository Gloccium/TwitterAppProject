package com.ziminpro.twitter.controllers;

import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Message;
import com.ziminpro.twitter.security.JwtService;
import com.ziminpro.twitter.services.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
public class MessageController {

    @Autowired
    private MessagesService messages;

    @Autowired
    private JwtService jwtService;

    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_MESSAGE + "/{message-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagebyId(
            @PathVariable(value = "message-id", required = true) String messageId) {
        return messages.getMessagebyId(UUID.fromString(messageId));
    }

    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_PRODUCER + "/{producer-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForProducerById(
            @PathVariable(value = "producer-id", required = true) String producerId) {
        return messages.getMessagesForProducerById(UUID.fromString(producerId));
    }

    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_SUBSCRIBER + "/{subscriber-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForSubscriberById(
            @PathVariable(value = "subscriber-id", required = true) String subscriberId) {
        return messages.getMessagesForSubscriberById(UUID.fromString(subscriberId));
    }

    @RequestMapping(method = RequestMethod.POST, path = Constants.URI_MESSAGE, consumes = Constants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, Object>>> createMessage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody Message message) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return messages.createMessage(message);
        }

        String token = authorization.substring(7);
        String userId = jwtService.extractUserId(token);
        if (userId != null) {
            try {
                // подставляем автора из токена
                message.setAuthor(UUID.fromString(userId));
            } catch (IllegalArgumentException e) {
                // если вдруг subject не UUID, оставляем как есть
            }
        }

        return messages.createMessage(message);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = Constants.URI_MESSAGE + "/{message-id}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteMessageById(
            @PathVariable(value = "message-id", required = true) String messageId) {
        return messages.deleteMessageById(UUID.fromString(messageId));
    }

}