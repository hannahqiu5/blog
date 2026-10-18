package com.hanqiu.blog.controller;


import com.hanqiu.blog.domain.dtos.LoginRequest;
import com.hanqiu.blog.domain.dtos.LoginResponse;
import com.hanqiu.blog.domain.dtos.RegisterRequest;
import com.hanqiu.blog.domain.dtos.RegisterResponse;
import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.services.AuthenticationService;
import com.hanqiu.blog.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request
    ) {
        User registeredUser = authenticationService.register(request);
        RegisterResponse response = RegisterResponse.builder()
                .id(registeredUser.getId())
                .username(registeredUser.getName())
                .email(registeredUser.getEmail())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
