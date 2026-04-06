package com.todoappma.authservice.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterRequestDto {
    private String email;
    private String password;
}
