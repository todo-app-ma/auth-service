package com.todoappma.authservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoappma.authservice.config.JwtConfig;
import com.todoappma.authservice.dto.request.LoginRequestDto;
import com.todoappma.authservice.dto.request.RegisterRequestDto;
import com.todoappma.authservice.dto.response.LoginResponseDto;
import com.todoappma.authservice.dto.response.RegisterResponseDto;
import com.todoappma.authservice.entity.Outbox;
import com.todoappma.authservice.entity.User;
import com.todoappma.authservice.exception.AuthException;
import com.todoappma.authservice.repository.OutboxRepository;
import com.todoappma.authservice.repository.UserRepository;
import com.todoappma.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailAlreadyTaken();
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        writeOutboxEvent(user.getId().toString(), "User", "UserRegistered",
                Map.of("userId", user.getId().toString(), "email", user.getEmail()));

        return RegisterResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(AuthException::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        String accessToken = jwtConfig.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtConfig.generateRefreshToken(user.getId());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    @SneakyThrows
    private void writeOutboxEvent(String aggregateId, String aggregateType, String eventType, Map<String, Object> payload) {
        outboxRepository.save(Outbox.builder()
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .eventType(eventType)
                .payload(objectMapper.writeValueAsString(payload))
                .createdAt(LocalDateTime.now())
                .build());
    }
}
