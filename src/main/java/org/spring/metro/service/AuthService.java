package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.config.JwtTokenProvider;
import org.spring.metro.models.entity.User;
import org.spring.metro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public User register(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    public String login(String email, String password) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        return jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }
}