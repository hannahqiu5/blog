package com.hanqiu.blog.services;

import com.hanqiu.blog.domain.dtos.LoginRequest;
import com.hanqiu.blog.domain.dtos.LoginResponse;
import com.hanqiu.blog.domain.dtos.RegisterRequest;
import com.hanqiu.blog.domain.entities.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    UserDetails authenticate(String email, String password);

    String generateToken(UserDetails userDetails);

    UserDetails validateToken(String token);


    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
