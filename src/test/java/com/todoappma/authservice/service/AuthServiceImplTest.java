package com.todoappma.authservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoappma.authservice.config.JwtConfig;
import com.todoappma.authservice.dto.request.LoginRequestDto;
import com.todoappma.authservice.dto.request.RegisterRequestDto;
import com.todoappma.authservice.entity.User;
import com.todoappma.authservice.exception.AuthException;
import com.todoappma.authservice.repository.OutboxRepository;
import com.todoappma.authservice.repository.UserRepository;
import com.todoappma.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private OutboxRepository outboxRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtConfig jwtConfig;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldSaveUserAndWriteOutboxEvent() throws Exception {
        var request = RegisterRequestDto.builder()
                .email("user@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        var savedUser = User.builder().email(request.getEmail()).passwordHash("hashed").build();
        when(userRepository.save(any())).thenReturn(savedUser);

        var response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("user@test.com");
        verify(outboxRepository).save(any());
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyTaken() {
        var request = RegisterRequestDto.builder().email("taken@test.com").password("pass").build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        var request = LoginRequestDto.builder().email("user@test.com").password("password123").build();
        var userId = UUID.randomUUID();
        var user = User.builder().email("user@test.com").passwordHash("hashed").build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtConfig.generateAccessToken(any(), any())).thenReturn("access-token");
        when(jwtConfig.generateRefreshToken(any())).thenReturn("refresh-token");

        var response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        var request = LoginRequestDto.builder().email("nobody@test.com").password("pass").build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        var request = LoginRequestDto.builder().email("user@test.com").password("wrong").build();
        var user = User.builder().email("user@test.com").passwordHash("hashed").build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid");
    }
}
