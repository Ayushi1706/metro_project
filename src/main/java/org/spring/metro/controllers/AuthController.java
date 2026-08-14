package org.spring.metro.controllers;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.User;
import org.spring.metro.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @RequestBody User user) {

        User savedUser = authService.register(user);
        savedUser.setPassword(null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User user) {

        String token = authService.login(
                user.getEmail(),
                user.getPassword()
        );

        return ResponseEntity.ok(
                Map.of("token", token)
        );
    }
}