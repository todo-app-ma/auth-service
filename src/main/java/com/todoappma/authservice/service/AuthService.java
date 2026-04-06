package com.todoappma.authservice.service;

import com.todoappma.authservice.dto.request.LoginRequestDto;
import com.todoappma.authservice.dto.request.RegisterRequestDto;
import com.todoappma.authservice.dto.response.LoginResponseDto;
import com.todoappma.authservice.dto.response.RegisterResponseDto;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto request);
    LoginResponseDto login(LoginRequestDto request);
}
