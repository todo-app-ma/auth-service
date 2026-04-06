package com.todoappma.authservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String email;
}
