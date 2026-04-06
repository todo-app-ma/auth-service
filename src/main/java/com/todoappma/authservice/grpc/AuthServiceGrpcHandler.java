package com.todoappma.authservice.grpc;

import com.todoappma.authservice.dto.request.LoginRequestDto;
import com.todoappma.authservice.dto.request.RegisterRequestDto;
import com.todoappma.authservice.exception.AuthException;
import com.todoappma.authservice.service.AuthService;
import com.todoappma.proto.auth.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthServiceGrpcHandler extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    @Override
    public void register(RegisterRequestGrpc request, StreamObserver<RegisterResponseGrpc> responseObserver) {
        try {
            var result = authService.register(RegisterRequestDto.builder()
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .build());

            responseObserver.onNext(RegisterResponseGrpc.newBuilder()
                    .setUserId(result.getUserId().toString())
                    .setEmail(result.getEmail())
                    .build());
            responseObserver.onCompleted();
        } catch (AuthException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getErrorCode() + ":" + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void login(LoginRequestGrpc request, StreamObserver<LoginResponseGrpc> responseObserver) {
        try {
            var result = authService.login(LoginRequestDto.builder()
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .build());

            responseObserver.onNext(LoginResponseGrpc.newBuilder()
                    .setAccessToken(result.getAccessToken())
                    .setRefreshToken(result.getRefreshToken())
                    .setUserId(result.getUserId().toString())
                    .setEmail(result.getEmail())
                    .build());
            responseObserver.onCompleted();
        } catch (AuthException e) {
            responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription(e.getErrorCode() + ":" + e.getMessage())
                    .asRuntimeException());
        }
    }
}
