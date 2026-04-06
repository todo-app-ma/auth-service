package com.todoappma.authservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RegisterResponseDto {
    private UUID userId;
    private String email;
}
