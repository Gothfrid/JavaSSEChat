/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simplechat.resources;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simplechat.entities.Message;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author bohdan
 */
@Controller
@SpringBootApplication
public class ChatResources {

    private static final Logger log = Logger.getLogger(ChatResources.class);

    private final Cache<String, SseEmitter> emCache = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .build();

    public static void main(String[] args) {
        SpringApplication.run(ChatResources.class, args);
    }

    @RequestMapping(path = "/connect", method = RequestMethod.GET)
    public SseEmitter connect(@RequestParam("token") String token) throws IOException {
        log.info(emCache.asMap().get(token));
        return emCache.asMap().get(token);
    }

    @ResponseBody
    @RequestMapping(path = "/callback", method = RequestMethod.GET)
    public String validate(@RequestParam("token") String token) throws IOException {
        emCache.getIfPresent(token);
        return "active";
    }

    @ResponseBody
    @RequestMapping(path = "/chat", method = RequestMethod.POST)
    public ResponseEntity<?> sendMessage(@Valid Message message) {

        log.info("Recieved: " + message);
        if (emCache.getIfPresent(message.getToken()) == null) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
        emCache.asMap().forEach((String token, SseEmitter emitter) -> {
            try {
                message.setToken("");
                emitter.send(message, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                emitter.complete();
                emCache.invalidate(token);
                e.printStackTrace();
            }
        });
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(String name, HttpServletResponse response) throws IOException {
        SseEmitter emitter = new SseEmitter();
        String token = JWT.create().withIssuer(name).sign(Algorithm.HMAC256("secret"));
        emCache.put(token, emitter);
        emitter.onCompletion(() -> this.emCache.invalidate(token));
        response.setHeader("token", token);
        return token;
    }
}
